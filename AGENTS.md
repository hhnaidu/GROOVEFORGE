# GrooveForge Records Rules

- Keep the project tracking-only. Do not add price calculation, automatic stock updates, or automatic batch-status changes.
- Use the required identifiers: `VINYL_RELEASE`, `CUSTOMER_ORDER`, `disc_condition`, and `row_label`.
- Put all SQL scripts in `sql/`.
- Put all Java source files in `src/grooveforge/`.
- Use `PreparedStatement` for every SQL value.
- Table and column identifiers must come only from the fixed allowlist in `Model.java`, never from user input.
- Do not invent tables, foreign keys, attributes, or business logic not specified in `docs/manual.txt`.
