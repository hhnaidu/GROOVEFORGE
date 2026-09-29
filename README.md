# GrooveForge Records

Java Swing + JDBC + Oracle tracking application for the GrooveForge Records DBMS capstone. The database records manually entered quantities, capacity, and statuses only. It does not calculate prices, adjust stock, or infer production outcomes.

## Layout

- `sql/`: Oracle user setup, schema, seed data, reports, verification, acceptance tests, and rollback demo.
- `src/grooveforge/`: Java 8 source, including one metadata-driven CRUD engine for all 14 tables.
- `lib/`: Oracle JDBC JAR. `ojdbc11.jar` is supplied locally for this project.
- `docs/manual.txt`: searchable implementation-manual text.
- `evidence/`: destination for lab captures.

## Oracle Setup

Start Oracle XE if using the local Docker environment:

```bash
docker run -d --name grooveforge-oracle-xe -p 1521:1521 -e ORACLE_PASSWORD=admin gvenzl/oracle-xe:21-slim
```

The local PDB JDBC URL is `jdbc:oracle:thin:@//localhost:1521/XEPDB1`.

Connect as an administrator to the intended PDB and run `sql/00_create_user.sql`. It prompts for the `GROOVEFORGE` password. Then reconnect as that user and, from the repository root, run:

```sql
@build.sql
@sql/05_acceptance_tests.sql
@sql/06_transaction_demo.sql
```

`build.sql` creates the schema, seed data, reports, and verification output. It stops on the first SQL error. Build it on a fresh project schema because Oracle DDL commits.

## Java Build And Run

Windows:

```bat
build.cmd
run.cmd
```

Linux/macOS:

```bash
chmod +x build.sh run.sh
./build.sh
./run.sh
```

For the console connection checkpoint:

```bash
java -cp 'build:lib/*' grooveforge.TestConnection
```

On Windows, replace `:` with `;` in the classpath. The application loads `oracle.jdbc.OracleDriver` by class name, so compilation needs only a JDK; running needs the JDBC JAR.

## Form Behavior

The login screen validates driver availability, credentials, and accessibility of all 14 tables before opening the dashboard. Every module is generated from `Model.java`; table and column names never come from form input. Foreign keys are ID-and-label selections, enumerations are drop-downs, and optional blank values are bound as SQL `NULL`.

Dates require `YYYY-MM-DD`; timestamps require `YYYY-MM-DD HH:MM:SS`. Required, format, string-length, and numeric-precision validation occurs before a JDBC write. Writes use prepared statements, a sequence-generated ID, `setAutoCommit(false)`, commit on success, rollback on failure, and a `SwingWorker` so the UI remains responsive.

## Lab Evidence Checklist

1. JDK and compiler versions.
2. Oracle version, connected `GROOVEFORGE` user, and JDBC URL/service.
3. `04_verify.sql` output showing 14 tables, 14 sequences, seed counts, and enabled constraints.
4. `05_acceptance_tests.sql` output ending with `ALL 14 ACCEPTANCE CHECKS PASSED`.
5. Login success plus rejected bad-credential and missing-driver/schema cases.
6. Add, search, update, and delete in all 14 modules.
7. Matching selected-master update and rejected mismatching master update.
8. At least three tracking report result sets.
9. `06_transaction_demo.sql` before, temporary Quality Hold, and after-rollback output.
10. Disconnect/reconnect persistence after an update.
