package grooveforge;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fixed metadata allowlist for every database identifier used by the UI. */
public final class Model {
    public enum ValueType { STRING, NUMBER, DATE, TIMESTAMP }

    public static final class ForeignKey {
        public final String table;
        public final String idColumn;
        public final String labelColumn;

        ForeignKey(String table, String idColumn, String labelColumn) {
            this.table = table;
            this.idColumn = idColumn;
            this.labelColumn = labelColumn;
        }
    }

    public static final class Field {
        public final String name;
        public final ValueType type;
        public final boolean required;
        public final int maxLength;
        public final int precision;
        public final int scale;
        public final List<String> enumValues;
        public final ForeignKey parent;

        Field(String name, ValueType type, boolean required, int maxLength, int precision,
              int scale, List<String> enumValues, ForeignKey parent) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.maxLength = maxLength;
            this.precision = precision;
            this.scale = scale;
            this.enumValues = enumValues;
            this.parent = parent;
        }
    }

    public static final class TableMeta {
        public final String table;
        public final String idColumn;
        public final String sequence;
        public final List<Field> fields;

        TableMeta(String table, String idColumn, String sequence, Field... fields) {
            this.table = table;
            this.idColumn = idColumn;
            this.sequence = sequence;
            this.fields = Collections.unmodifiableList(Arrays.asList(fields));
        }

        public Field field(String name) {
            for (Field field : fields) {
                if (field.name.equals(name)) return field;
            }
            return null;
        }
    }

    public static final class Report {
        public final String title;
        public final String sql;

        Report(String title, String sql) {
            this.title = title;
            this.sql = sql;
        }
    }

    private static final Map<String, TableMeta> TABLES = new LinkedHashMap<String, TableMeta>();
    private static final List<Report> REPORTS;

    static {
        add(table("PRESSING_MACHINE", "machine_id", "seq_pressing_machine",
            n("machine_id", true, 9), s("machine_name", true, 80), s("model_number", true, 60),
            d("installed_date", true), d("last_maintenance_date", false),
            e("status", true, 25, "Operational", "Under Maintenance", "Faulty", "Retired")));
        add(table("MAINTENANCE_LOG", "log_id", "seq_maintenance_log",
            n("log_id", true, 9), fk("machine_id", true, "PRESSING_MACHINE", "machine_id", "machine_name"),
            d("maintenance_date", true), s("issue_reported", true, 250), s("action_taken", true, 250),
            s("performed_by", true, 100), d("next_due_date", false)));
        add(table("RAW_MATERIAL", "material_id", "seq_raw_material",
            n("material_id", true, 9), s("material_name", true, 100), s("supplier_name", true, 100),
            dec("quantity_in_stock", true, 12, 3),
            e("unit", true, 12, "Kilograms", "Sheets", "Rolls", "Pieces"),
            d("last_restocked_date", false), s("storage_location", true, 100)));
        add(table("RESTOCK_LOG", "restock_id", "seq_restock_log",
            n("restock_id", true, 9), fk("material_id", true, "RAW_MATERIAL", "material_id", "material_name"),
            dec("quantity_added", true, 12, 3), d("restocked_date", true), s("supplier_name", true, 100),
            s("invoice_number", true, 60), s("received_by", true, 100)));
        add(table("ARTIST", "artist_id", "seq_artist",
            n("artist_id", true, 9), s("artist_name", true, 100), s("genre", true, 60),
            s("contact_person", true, 100), s("phone", true, 25), s("email", true, 120),
            d("contract_start_date", true), d("contract_end_date", false)));
        add(table("VINYL_RELEASE", "release_id", "seq_vinyl_release",
            n("release_id", true, 9), fk("artist_id", true, "ARTIST", "artist_id", "artist_name"),
            s("album_title", true, 150), e("format", true, 10, "7 Inch", "10 Inch", "12 Inch", "LP", "EP"),
            s("color_variant", true, 60), n("speed_rpm", true, 2), n("total_tracks", true, 3),
            dec("estimated_runtime_minutes", true, 6, 2),
            fk("master_vinyl_disc_id", false, "MASTER_DISC", "disc_id", "disc_name")));
        add(table("MASTER_DISC", "disc_id", "seq_master_disc",
            n("disc_id", true, 9), fk("release_id", true, "VINYL_RELEASE", "release_id", "album_title"),
            s("disc_name", true, 100), d("created_date", true), s("created_by", true, 100),
            s("storage_location", true, 100), e("disc_condition", true, 10, "New", "Good", "Worn", "Damaged")));
        add(table("PRODUCTION_BATCH", "batch_id", "seq_production_batch",
            n("batch_id", true, 9), fk("release_id", true, "VINYL_RELEASE", "release_id", "album_title"),
            fk("machine_id", true, "PRESSING_MACHINE", "machine_id", "machine_name"), n("batch_size", true, 9),
            ts("started_at", false), ts("completed_at", false), s("supervisor_name", true, 100),
            e("status", true, 15, "Scheduled", "In Progress", "Completed", "Quality Hold", "Rejected")));
        add(table("BATCH_MATERIAL_USAGE", "usage_id", "seq_batch_material_usage",
            n("usage_id", true, 9), fk("batch_id", true, "PRODUCTION_BATCH", "batch_id", "batch_id"),
            fk("material_id", true, "RAW_MATERIAL", "material_id", "material_name"),
            dec("quantity_used", true, 12, 3), e("unit", true, 12, "Kilograms", "Sheets", "Rolls", "Pieces")));
        add(table("QUALITY_CHECK", "check_id", "seq_quality_check",
            n("check_id", true, 9), fk("batch_id", true, "PRODUCTION_BATCH", "batch_id", "batch_id"),
            n("sample_size", true, 9), s("checked_by", true, 100), ts("checked_at", true),
            e("visual_defects_found", true, 25, "Scratches", "Warping", "Bubbles", "Color Inconsistency", "None"),
            e("audio_defects_found", true, 15, "Pops", "Clicks", "Skips", "Distortion", "None"),
            e("overall_result", true, 20, "Pass", "Conditional Pass", "Fail"), s("notes", false, 500)));
        add(table("FINISHED_RECORD", "record_id", "seq_finished_record",
            n("record_id", true, 9), fk("batch_id", true, "PRODUCTION_BATCH", "batch_id", "batch_id"),
            fk("release_id", true, "VINYL_RELEASE", "release_id", "album_title"), s("serial_number", true, 60),
            e("sleeve_type", true, 25, "Standard", "Gatefold", "Picture Disc Sleeve"),
            e("jacket_condition", true, 12, "Mint", "Near Mint", "Good", "Fair"), s("current_location", true, 100)));
        add(table("CUSTOMER_ORDER", "order_id", "seq_customer_order",
            n("order_id", true, 9), fk("release_id", true, "VINYL_RELEASE", "release_id", "album_title"),
            s("customer_name", true, 100), n("quantity_ordered", true, 9), d("order_date", true),
            e("status", true, 15, "Pending", "In Production", "Quality Hold", "Ready to Ship", "Shipped", "Delivered"),
            s("shipping_address", true, 250), d("shipped_date", false), s("tracking_number", false, 80)));
        add(table("WAREHOUSE_LOCATION", "location_id", "seq_warehouse_location",
            n("location_id", true, 9), s("shelf_number", true, 20), s("row_label", true, 20),
            s("section", true, 40), n("current_capacity_used", true, 9), n("max_capacity", true, 9)));
        add(table("EMPLOYEE", "employee_id", "seq_employee",
            n("employee_id", true, 9), s("name", true, 100),
            e("role", true, 20, "Press Operator", "Quality Inspector", "Warehouse Staff", "Supervisor", "Admin"),
            e("shift", true, 10, "Morning", "Evening", "Night"), s("phone", true, 25), d("date_joined", true)));

        REPORTS = Collections.unmodifiableList(Arrays.asList(
            report("Release catalog", "SELECT r.release_id, a.artist_name, r.album_title, r.format, r.color_variant FROM VINYL_RELEASE r JOIN ARTIST a ON a.artist_id = r.artist_id ORDER BY r.release_id"),
            report("Batch tracking", "SELECT b.batch_id, r.album_title, m.machine_name, b.status, b.batch_size FROM PRODUCTION_BATCH b JOIN VINYL_RELEASE r ON r.release_id = b.release_id JOIN PRESSING_MACHINE m ON m.machine_id = b.machine_id ORDER BY b.batch_id"),
            report("Quality history", "SELECT q.check_id, r.album_title, q.batch_id, q.overall_result, q.notes FROM QUALITY_CHECK q JOIN PRODUCTION_BATCH b ON b.batch_id = q.batch_id JOIN VINYL_RELEASE r ON r.release_id = b.release_id ORDER BY q.check_id"),
            report("Material usage", "SELECT u.usage_id, u.batch_id, m.material_name, u.quantity_used, u.unit FROM BATCH_MATERIAL_USAGE u JOIN RAW_MATERIAL m ON m.material_id = u.material_id ORDER BY u.usage_id"),
            report("Orders awaiting dispatch", "SELECT order_id, customer_name, release_id, status FROM CUSTOMER_ORDER WHERE status IN ('Pending', 'In Production', 'Quality Hold', 'Ready to Ship') ORDER BY order_id"),
            report("Record traceability", "SELECT f.serial_number, r.album_title, b.batch_id, m.machine_name, f.current_location FROM FINISHED_RECORD f JOIN PRODUCTION_BATCH b ON b.batch_id = f.batch_id JOIN VINYL_RELEASE r ON r.release_id = f.release_id JOIN PRESSING_MACHINE m ON m.machine_id = b.machine_id ORDER BY f.record_id"),
            report("Maintenance follow-up", "SELECT m.machine_name, l.maintenance_date, l.issue_reported, l.next_due_date FROM MAINTENANCE_LOG l JOIN PRESSING_MACHINE m ON m.machine_id = l.machine_id ORDER BY l.log_id"),
            report("Selected master discs", "SELECT r.release_id, r.album_title, d.disc_name, d.disc_condition FROM VINYL_RELEASE r LEFT JOIN MASTER_DISC d ON d.disc_id = r.master_vinyl_disc_id AND d.release_id = r.release_id ORDER BY r.release_id"),
            report("Releases without batches", "SELECT r.release_id, r.album_title FROM VINYL_RELEASE r WHERE NOT EXISTS (SELECT 1 FROM PRODUCTION_BATCH b WHERE b.release_id = r.release_id) ORDER BY r.release_id"),
            report("Failed quality checks", "SELECT check_id, batch_id, visual_defects_found, audio_defects_found, notes FROM QUALITY_CHECK WHERE overall_result = 'Fail' ORDER BY check_id")
        ));
    }

    private Model() { }
    public static Map<String, TableMeta> tables() { return Collections.unmodifiableMap(TABLES); }
    public static TableMeta table(String name) { return TABLES.get(name); }
    public static List<Report> reports() { return REPORTS; }

    private static void add(TableMeta table) { TABLES.put(table.table, table); }
    private static TableMeta table(String name, String id, String sequence, Field... fields) { return new TableMeta(name, id, sequence, fields); }
    private static Field s(String name, boolean required, int length) { return new Field(name, ValueType.STRING, required, length, 0, 0, Collections.<String>emptyList(), null); }
    private static Field n(String name, boolean required, int precision) { return new Field(name, ValueType.NUMBER, required, 0, precision, 0, Collections.<String>emptyList(), null); }
    private static Field dec(String name, boolean required, int precision, int scale) { return new Field(name, ValueType.NUMBER, required, 0, precision, scale, Collections.<String>emptyList(), null); }
    private static Field d(String name, boolean required) { return new Field(name, ValueType.DATE, required, 0, 0, 0, Collections.<String>emptyList(), null); }
    private static Field ts(String name, boolean required) { return new Field(name, ValueType.TIMESTAMP, required, 0, 0, 0, Collections.<String>emptyList(), null); }
    private static Field e(String name, boolean required, int length, String... values) { return new Field(name, ValueType.STRING, required, length, 0, 0, Collections.unmodifiableList(Arrays.asList(values)), null); }
    private static Field fk(String name, boolean required, String table, String id, String label) { return new Field(name, ValueType.NUMBER, required, 0, 9, 0, Collections.<String>emptyList(), new ForeignKey(table, id, label)); }
    private static Report report(String title, String sql) { return new Report(title, sql); }
}
