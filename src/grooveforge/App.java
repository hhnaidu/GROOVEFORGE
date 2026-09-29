package grooveforge;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/** Metadata-driven Java 8 Swing CRUD client for GrooveForge Records. */
public final class App extends JFrame {
    private static final String LOGIN = "login";
    private static final String DASHBOARD = "dashboard";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JTextField urlField = new JTextField("jdbc:oracle:thin:@//localhost:1521/XEPDB1", 35);
    private final JTextField usernameField = new JTextField("grooveforge", 18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton connectButton = new JButton("Connect");
    private final JComboBox<String> moduleBox = new JComboBox<String>();
    private final JButton reportsButton = new JButton("Tracking reports");
    private final JButton disconnectButton = new JButton("Disconnect");
    private final JButton addButton = new JButton("Add new");
    private final JButton searchButton = new JButton("Search by ID");
    private final JButton updateButton = new JButton("Update selected");
    private final JButton deleteButton = new JButton("Delete selected");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");
    private final JPanel formPanel = new JPanel(new GridBagLayout());
    private final JTable results = new JTable();
    private final Map<String, JComponent> editors = new LinkedHashMap<String, JComponent>();
    private final List<JComponent> busyControls = new ArrayList<JComponent>();
    private Connection connection;
    private Model.TableMeta current;
    private Long selectedId;

    public App() {
        super("GrooveForge Records");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 620));
        cardPanel.add(createLogin(), LOGIN);
        cardPanel.add(createDashboard(), DASHBOARD);
        add(cardPanel);
        cards.show(cardPanel, LOGIN);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        GridBagConstraints c = constraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        panel.add(new JLabel("GrooveForge Records JDBC sign-in"), c);
        addLoginRow(panel, 1, "JDBC URL", urlField);
        addLoginRow(panel, 2, "Username", usernameField);
        addLoginRow(panel, 3, "Password", passwordField);
        c.gridy = 4;
        c.gridx = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        panel.add(connectButton, c);
        connectButton.addActionListener(e -> connect());
        passwordField.addActionListener(e -> connect());
        busyControls.add(urlField);
        busyControls.add(usernameField);
        busyControls.add(passwordField);
        busyControls.add(connectButton);
        return panel;
    }

    private void addLoginRow(JPanel panel, int row, String label, JComponent editor) {
        GridBagConstraints c = constraints();
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label + ":"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(editor, c);
    }

    private JPanel createDashboard() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Module:"));
        for (String table : Model.tables().keySet()) moduleBox.addItem(table);
        top.add(moduleBox);
        top.add(reportsButton);
        top.add(disconnectButton);
        panel.add(top, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 3, 6, 6));
        actions.add(addButton);
        actions.add(searchButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(clearButton);
        actions.add(refreshButton);
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        left.add(actions, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, new JScrollPane(results));
        split.setResizeWeight(0.36);
        panel.add(split, BorderLayout.CENTER);

        moduleBox.addActionListener(e -> moduleChanged());
        reportsButton.addActionListener(e -> showReports());
        disconnectButton.addActionListener(e -> disconnect());
        addButton.addActionListener(e -> addNew());
        searchButton.addActionListener(e -> search());
        updateButton.addActionListener(e -> updateSelected());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> reloadCurrent());
        results.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
        busyControls.add(moduleBox);
        busyControls.add(reportsButton);
        busyControls.add(disconnectButton);
        busyControls.add(addButton);
        busyControls.add(searchButton);
        busyControls.add(updateButton);
        busyControls.add(deleteButton);
        busyControls.add(clearButton);
        busyControls.add(refreshButton);
        return panel;
    }

    private void connect() {
        final String url = urlField.getText().trim();
        final String username = usernameField.getText().trim();
        final String password = new String(passwordField.getPassword());
        passwordField.setText("");
        if (url.length() == 0 || username.length() == 0 || password.length() == 0) {
            showError("JDBC URL, username, and password are required.");
            return;
        }
        setBusy(true);
        new SwingWorker<Connection, Void>() {
            private String problem;
            protected Connection doInBackground() {
                try {
                    Class.forName("oracle.jdbc.OracleDriver");
                } catch (ClassNotFoundException exception) {
                    problem = "Oracle JDBC driver is missing. Put ojdbc11.jar in lib/.";
                    return null;
                }
                try {
                    Connection candidate = DriverManager.getConnection(url, username, password);
                    List<String> missing = TestConnection.missingTables(candidate);
                    if (!missing.isEmpty()) {
                        candidate.close();
                        problem = "Project tables are missing or inaccessible: " + missing;
                        return null;
                    }
                    candidate.setAutoCommit(false);
                    return candidate;
                } catch (SQLException exception) {
                    problem = exception.getErrorCode() == 1017
                            ? "Bad credentials: Oracle rejected the username or password."
                            : "Connection failed: " + exception.getMessage();
                    return null;
                }
            }
            protected void done() {
                setBusy(false);
                try {
                    connection = get();
                    if (connection == null) {
                        showError(problem);
                        return;
                    }
                    cards.show(cardPanel, DASHBOARD);
                    moduleChanged();
                } catch (Exception exception) {
                    showError("Connection failed: " + rootMessage(exception));
                }
            }
        }.execute();
    }

    private void disconnect() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException exception) {
            showError("Disconnect failed: " + exception.getMessage());
        } finally {
            connection = null;
            selectedId = null;
            cards.show(cardPanel, LOGIN);
        }
    }

    private void moduleChanged() {
        String table = (String) moduleBox.getSelectedItem();
        current = Model.table(table);
        buildForm();
        reloadCurrent();
    }

    private void buildForm() {
        formPanel.removeAll();
        editors.clear();
        selectedId = null;
        int row = 0;
        for (Model.Field field : current.fields) {
            GridBagConstraints c = constraints();
            c.gridx = 0;
            c.gridy = row;
            c.anchor = GridBagConstraints.EAST;
            formPanel.add(new JLabel(displayName(field.name) + (field.required ? " *" : "") + ":"), c);
            JComponent editor = editorFor(field);
            editors.put(field.name, editor);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            formPanel.add(editor, c);
            row++;
        }
        GridBagConstraints filler = constraints();
        filler.gridx = 0;
        filler.gridy = row;
        filler.weighty = 1;
        formPanel.add(new JPanel(), filler);
        formPanel.revalidate();
        formPanel.repaint();
    }

    private JComponent editorFor(Model.Field field) {
        if (field.parent != null || !field.enumValues.isEmpty()) {
            JComboBox<Option> combo = new JComboBox<Option>();
            combo.addItem(new Option(null, ""));
            if (field.parent == null) {
                for (String value : field.enumValues) combo.addItem(new Option(value, value));
            }
            return combo;
        }
        JTextField text = new JTextField(20);
        if (field.name.equals(current.idColumn)) text.setToolTipText("Enter an ID to search; generated for new rows.");
        return text;
    }

    private void reloadCurrent() {
        if (connection == null || current == null) return;
        final Model.TableMeta meta = current;
        setBusy(true);
        new SwingWorker<LoadedModule, Void>() {
            protected LoadedModule doInBackground() throws Exception {
                return loadModule(meta);
            }
            protected void done() {
                setBusy(false);
                try {
                    if (meta != current) return;
                    applyLoadedModule(get());
                } catch (Exception exception) {
                    showError("Refresh failed: " + rootMessage(exception));
                }
            }
        }.execute();
    }

    private LoadedModule loadModule(Model.TableMeta meta) throws SQLException {
        LoadedModule loaded = new LoadedModule();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + meta.table + " ORDER BY " + meta.idColumn);
             ResultSet result = statement.executeQuery()) {
            ResultSetMetaData md = result.getMetaData();
            for (int column = 1; column <= md.getColumnCount(); column++) loaded.columns.add(md.getColumnLabel(column));
            while (result.next()) {
                Object[] row = new Object[md.getColumnCount()];
                for (int column = 1; column <= md.getColumnCount(); column++) row[column - 1] = result.getObject(column);
                loaded.rows.add(row);
            }
        }
        for (Model.Field field : meta.fields) {
            if (field.parent == null) continue;
            List<Option> options = new ArrayList<Option>();
            String sql = "SELECT " + field.parent.idColumn + ", " + field.parent.labelColumn + " FROM "
                    + field.parent.table + " ORDER BY " + field.parent.idColumn;
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) options.add(new Option(result.getObject(1), result.getString(2)));
            }
            loaded.parents.put(field.name, options);
        }
        return loaded;
    }

    private void applyLoadedModule(LoadedModule loaded) {
        for (Map.Entry<String, List<Option>> entry : loaded.parents.entrySet()) {
            @SuppressWarnings("unchecked") JComboBox<Option> combo = (JComboBox<Option>) editors.get(entry.getKey());
            combo.removeAllItems();
            combo.addItem(new Option(null, ""));
            for (Option option : entry.getValue()) combo.addItem(option);
        }
        DefaultTableModel model = new DefaultTableModel(loaded.columns.toArray(), 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Object[] row : loaded.rows) model.addRow(row);
        results.setModel(model);
        clearForm();
    }

    private void addNew() {
        if (selectedId != null) {
            showError("Clear the selected row before adding a new one.");
            return;
        }
        write("Add failed", new DbWork() {
            public String execute() throws Exception {
                Map<String, Object> values = readValues(false);
                long id = nextId(current);
                insert(current, values, id);
                return "Added " + current.table + " ID " + id + ".";
            }
        });
    }

    private void search() {
        Long id;
        try {
            id = readId();
        } catch (ValidationException exception) {
            showError(exception.getMessage());
            return;
        }
        for (int row = 0; row < results.getRowCount(); row++) {
            Object value = results.getValueAt(row, 0);
            if (value != null && new BigDecimal(value.toString()).longValue() == id.longValue()) {
                results.setRowSelectionInterval(row, row);
                return;
            }
        }
        showError("ID " + id + " was not found in " + current.table + ".");
    }

    private void updateSelected() {
        if (selectedId == null) {
            showError("Select or search for a row before updating.");
            return;
        }
        write("Update failed", new DbWork() {
            public String execute() throws Exception {
                update(current, readValues(true), selectedId.longValue());
                return "Updated " + current.table + " ID " + selectedId + ".";
            }
        });
    }

    private void deleteSelected() {
        if (selectedId == null) {
            showError("Select or search for a row before deleting.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + current.table + " ID " + selectedId + "?", "Confirm delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        write("Delete failed", new DbWork() {
            public String execute() throws Exception {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + current.table + " WHERE " + current.idColumn + " = ?")) {
                    statement.setLong(1, selectedId.longValue());
                    if (statement.executeUpdate() != 1) throw new SQLException("Selected row was not deleted.");
                }
                return "Deleted " + current.table + " ID " + selectedId + ".";
            }
        });
    }

    private void write(final String failurePrefix, final DbWork work) {
        setBusy(true);
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                try {
                    String message = work.execute();
                    connection.commit();
                    return message;
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                }
            }
            protected void done() {
                setBusy(false);
                try {
                    showInfo(get());
                    reloadCurrent();
                } catch (Exception exception) {
                    showError(failurePrefix + ": " + rootMessage(exception));
                }
            }
        }.execute();
    }

    private long nextId(Model.TableMeta meta) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT " + meta.sequence + ".NEXTVAL FROM dual");
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        }
    }

    private void insert(Model.TableMeta meta, Map<String, Object> values, long id) throws SQLException {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Model.Field> bindFields = nonIdFields(meta);
        for (Model.Field field : bindFields) {
            if (columns.length() > 0) { columns.append(", "); placeholders.append(", "); }
            columns.append(field.name);
            placeholders.append("?");
        }
        columns.append(", ").append(meta.idColumn);
        placeholders.append(", ?");
        String sql = "INSERT INTO " + meta.table + " (" + columns + ") VALUES (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = bind(statement, bindFields, values, 1);
            statement.setLong(index, id);
            if (statement.executeUpdate() != 1) throw new SQLException("Insert did not affect one row.");
        }
    }

    private void update(Model.TableMeta meta, Map<String, Object> values, long id) throws SQLException {
        List<Model.Field> bindFields = nonIdFields(meta);
        StringBuilder set = new StringBuilder();
        for (Model.Field field : bindFields) {
            if (set.length() > 0) set.append(", ");
            set.append(field.name).append(" = ?");
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + meta.table + " SET " + set + " WHERE " + meta.idColumn + " = ?")) {
            int index = bind(statement, bindFields, values, 1);
            statement.setLong(index, id);
            if (statement.executeUpdate() != 1) throw new SQLException("Update did not affect one row.");
        }
    }

    private int bind(PreparedStatement statement, List<Model.Field> fields, Map<String, Object> values, int index) throws SQLException {
        for (Model.Field field : fields) {
            Object value = values.get(field.name);
            if (value == null) statement.setNull(index++, sqlType(field.type));
            else if (field.type == Model.ValueType.NUMBER) statement.setBigDecimal(index++, (BigDecimal) value);
            else if (field.type == Model.ValueType.DATE) statement.setDate(index++, (java.sql.Date) value);
            else if (field.type == Model.ValueType.TIMESTAMP) statement.setTimestamp(index++, (Timestamp) value);
            else statement.setString(index++, value.toString());
        }
        return index;
    }

    private Map<String, Object> readValues(boolean updating) throws ValidationException {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (Model.Field field : current.fields) {
            if (field.name.equals(current.idColumn)) continue;
            values.put(field.name, parse(field, rawValue(field)));
        }
        return values;
    }

    private Object rawValue(Model.Field field) {
        JComponent editor = editors.get(field.name);
        if (editor instanceof JTextField) return ((JTextField) editor).getText().trim();
        @SuppressWarnings("unchecked") JComboBox<Option> combo = (JComboBox<Option>) editor;
        Option option = (Option) combo.getSelectedItem();
        return option == null ? null : option.value;
    }

    private Object parse(Model.Field field, Object raw) throws ValidationException {
        String text = raw == null ? "" : raw.toString().trim();
        if (text.length() == 0) {
            if (field.required) throw new ValidationException(displayName(field.name) + " is required.");
            return null;
        }
        if (field.parent != null || !field.enumValues.isEmpty()) return raw;
        if (field.type == Model.ValueType.STRING) {
            if (text.length() > field.maxLength) throw new ValidationException(displayName(field.name) + " exceeds " + field.maxLength + " characters.");
            return text;
        }
        if (field.type == Model.ValueType.NUMBER) {
            try {
                BigDecimal number = new BigDecimal(text);
                if (number.precision() > field.precision || Math.max(0, number.scale()) > field.scale) {
                    throw new ValidationException(displayName(field.name) + " exceeds NUMBER(" + field.precision + "," + field.scale + ").");
                }
                return number;
            } catch (NumberFormatException exception) {
                throw new ValidationException(displayName(field.name) + " must be a number.");
            }
        }
        if (field.type == Model.ValueType.DATE) {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
                format.setLenient(false);
                Date date = format.parse(text);
                if (!format.format(date).equals(text)) throw new java.text.ParseException(text, 0);
                return new java.sql.Date(date.getTime());
            } catch (Exception exception) {
                throw new ValidationException(displayName(field.name) + " must use YYYY-MM-DD.");
            }
        }
        try {
            return Timestamp.valueOf(LocalDateTime.parse(text, TIMESTAMP_FORMAT));
        } catch (DateTimeParseException exception) {
            throw new ValidationException(displayName(field.name) + " must use YYYY-MM-DD HH:MM:SS.");
        }
    }

    private void loadSelectedRow() {
        int row = results.getSelectedRow();
        if (row < 0 || current == null) return;
        for (int column = 0; column < current.fields.size(); column++) setEditor(current.fields.get(column), results.getValueAt(row, column));
        Object id = results.getValueAt(row, 0);
        selectedId = id == null ? null : new BigDecimal(id.toString()).longValue();
    }

    private void setEditor(Model.Field field, Object value) {
        JComponent editor = editors.get(field.name);
        if (editor instanceof JTextField) {
            ((JTextField) editor).setText(value == null ? "" : value.toString());
            return;
        }
        @SuppressWarnings("unchecked") JComboBox<Option> combo = (JComboBox<Option>) editor;
        String wanted = value == null ? null : value.toString();
        for (int index = 0; index < combo.getItemCount(); index++) {
            Option option = combo.getItemAt(index);
            if ((wanted == null && option.value == null) || (option.value != null && option.value.toString().equals(wanted))) {
                combo.setSelectedIndex(index);
                return;
            }
        }
    }

    private void clearForm() {
        selectedId = null;
        results.clearSelection();
        for (Model.Field field : current.fields) setEditor(field, null);
    }

    private Long readId() throws ValidationException {
        String value = ((JTextField) editors.get(current.idColumn)).getText().trim();
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new ValidationException("Enter a positive numeric " + current.idColumn + ".");
        }
    }

    private List<Model.Field> nonIdFields(Model.TableMeta meta) {
        List<Model.Field> fields = new ArrayList<Model.Field>();
        for (Model.Field field : meta.fields) if (!field.name.equals(meta.idColumn)) fields.add(field);
        return fields;
    }

    private void showReports() {
        final JDialog dialog = new JDialog((Window) this, "Tracking reports", JDialog.ModalityType.MODELESS);
        final JComboBox<String> reportBox = new JComboBox<String>();
        for (Model.Report report : Model.reports()) reportBox.addItem(report.title);
        final JTable table = new JTable();
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(reportBox, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        reportBox.addActionListener(e -> loadReport((Model.Report) Model.reports().get(reportBox.getSelectedIndex()), table, reportBox));
        dialog.setContentPane(panel);
        dialog.setSize(900, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        loadReport(Model.reports().get(0), table, reportBox);
    }

    private void loadReport(final Model.Report report, final JTable table, final JComboBox<String> selector) {
        selector.setEnabled(false);
        new SwingWorker<LoadedModule, Void>() {
            protected LoadedModule doInBackground() throws Exception {
                LoadedModule loaded = new LoadedModule();
                try (PreparedStatement statement = connection.prepareStatement(report.sql); ResultSet result = statement.executeQuery()) {
                    ResultSetMetaData md = result.getMetaData();
                    for (int i = 1; i <= md.getColumnCount(); i++) loaded.columns.add(md.getColumnLabel(i));
                    while (result.next()) {
                        Object[] row = new Object[md.getColumnCount()];
                        for (int i = 1; i <= md.getColumnCount(); i++) row[i - 1] = result.getObject(i);
                        loaded.rows.add(row);
                    }
                }
                return loaded;
            }
            protected void done() {
                selector.setEnabled(true);
                try {
                    LoadedModule loaded = get();
                    DefaultTableModel model = new DefaultTableModel(loaded.columns.toArray(), 0) {
                        public boolean isCellEditable(int row, int column) { return false; }
                    };
                    for (Object[] row : loaded.rows) model.addRow(row);
                    table.setModel(model);
                } catch (Exception exception) {
                    showError("Report failed: " + rootMessage(exception));
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        for (JComponent control : busyControls) control.setEnabled(!busy);
        for (JComponent editor : editors.values()) editor.setEnabled(!busy);
    }
    private static int sqlType(Model.ValueType type) { return type == Model.ValueType.NUMBER ? Types.NUMERIC : type == Model.ValueType.DATE ? Types.DATE : type == Model.ValueType.TIMESTAMP ? Types.TIMESTAMP : Types.VARCHAR; }
    private static String displayName(String name) { return name.replace('_', ' '); }
    private static GridBagConstraints constraints() { GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4, 4, 4, 4); return c; }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "GrooveForge", JOptionPane.ERROR_MESSAGE); }
    private void showInfo(String message) { JOptionPane.showMessageDialog(this, message, "GrooveForge", JOptionPane.INFORMATION_MESSAGE); }
    private static String rootMessage(Exception exception) { Throwable cause = exception; while (cause.getCause() != null) cause = cause.getCause(); return cause.getMessage() == null ? cause.toString() : cause.getMessage(); }

    private interface DbWork { String execute() throws Exception; }
    private static final class ValidationException extends Exception { ValidationException(String message) { super(message); } }
    private static final class Option {
        final Object value; final String label;
        Option(Object value, String label) { this.value = value; this.label = label; }
        public String toString() { return value == null ? "" : value + " | " + label; }
    }
    private static final class LoadedModule {
        final List<String> columns = new ArrayList<String>();
        final List<Object[]> rows = new ArrayList<Object[]>();
        final Map<String, List<Option>> parents = new LinkedHashMap<String, List<Option>>();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() { public void run() { new App().setVisible(true); } });
    }
}
