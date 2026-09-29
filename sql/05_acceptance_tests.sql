WHENEVER SQLERROR EXIT SQL.SQLCODE
SET SERVEROUTPUT ON

SAVEPOINT a01;
BEGIN
  INSERT INTO VINYL_RELEASE VALUES (9001, 999999, 'Bad Parent', 'LP', 'Black', 33, 1, 1, NULL);
  RAISE_APPLICATION_ERROR(-20001, 'Missing artist parent: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2291 THEN RAISE_APPLICATION_ERROR(-20002, 'Missing artist parent: expected ORA-02291, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Missing artist parent rejected with ORA-02291');
END;
/
ROLLBACK TO a01;

SAVEPOINT a02;
BEGIN
  UPDATE VINYL_RELEASE SET master_vinyl_disc_id = 2 WHERE release_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Cross-release selected master: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2291 THEN RAISE_APPLICATION_ERROR(-20002, 'Cross-release selected master: expected ORA-02291, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Cross-release selected master rejected with ORA-02291');
END;
/
ROLLBACK TO a02;

SAVEPOINT a03;
BEGIN
  INSERT INTO FINISHED_RECORD VALUES (9003, 2, 1, 'GF-BAD-0003', 'Standard', 'Mint', 'Test');
  RAISE_APPLICATION_ERROR(-20001, 'Finished record mismatched release: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2291 THEN RAISE_APPLICATION_ERROR(-20002, 'Finished record mismatched release: expected ORA-02291, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Finished record mismatched release rejected with ORA-02291');
END;
/
ROLLBACK TO a03;

SAVEPOINT a04;
BEGIN
  INSERT INTO BATCH_MATERIAL_USAGE VALUES (9004, 1, 1, 1, 'Sheets');
  RAISE_APPLICATION_ERROR(-20001, 'Usage unit mismatch: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2291 THEN RAISE_APPLICATION_ERROR(-20002, 'Usage unit mismatch: expected ORA-02291, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Usage unit mismatch rejected with ORA-02291');
END;
/
ROLLBACK TO a04;

SAVEPOINT a05;
BEGIN
  UPDATE RAW_MATERIAL SET quantity_in_stock = -1 WHERE material_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Negative raw stock: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Negative raw stock: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Negative raw stock rejected with ORA-02290');
END;
/
ROLLBACK TO a05;

SAVEPOINT a06;
BEGIN
  INSERT INTO FINISHED_RECORD VALUES (9006, 1, 1, 'GF-CAR-0001', 'Standard', 'Mint', 'Test');
  RAISE_APPLICATION_ERROR(-20001, 'Duplicate serial: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -1 THEN RAISE_APPLICATION_ERROR(-20002, 'Duplicate serial: expected ORA-00001, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Duplicate serial rejected with ORA-00001');
END;
/
ROLLBACK TO a06;

SAVEPOINT a07;
BEGIN
  UPDATE PRESSING_MACHINE SET status = 'Broken' WHERE machine_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Invalid machine status: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Invalid machine status: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Invalid machine status rejected with ORA-02290');
END;
/
ROLLBACK TO a07;

SAVEPOINT a08;
BEGIN
  UPDATE ARTIST SET contract_end_date = DATE '2025-01-01' WHERE artist_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Bad contract chronology: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Bad contract chronology: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Bad contract chronology rejected with ORA-02290');
END;
/
ROLLBACK TO a08;

SAVEPOINT a09;
BEGIN
  UPDATE PRODUCTION_BATCH SET status = 'Completed', started_at = NULL, completed_at = NULL WHERE batch_id = 2;
  RAISE_APPLICATION_ERROR(-20001, 'Completion without start: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Completion without start: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Completion without start rejected with ORA-02290');
END;
/
ROLLBACK TO a09;

SAVEPOINT a10;
BEGIN
  UPDATE CUSTOMER_ORDER SET status = 'Shipped', shipped_date = NULL, tracking_number = NULL WHERE order_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Shipment details missing: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Shipment details missing: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Shipment details missing rejected with ORA-02290');
END;
/
ROLLBACK TO a10;

SAVEPOINT a11;
BEGIN
  UPDATE WAREHOUSE_LOCATION SET current_capacity_used = max_capacity + 1 WHERE location_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Warehouse over capacity: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2290 THEN RAISE_APPLICATION_ERROR(-20002, 'Warehouse over capacity: expected ORA-02290, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Warehouse over capacity rejected with ORA-02290');
END;
/
ROLLBACK TO a11;

SAVEPOINT a12;
BEGIN
  DELETE FROM PRESSING_MACHINE WHERE machine_id = 1;
  RAISE_APPLICATION_ERROR(-20001, 'Delete referenced machine: invalid write succeeded');
EXCEPTION WHEN OTHERS THEN
  IF SQLCODE = -20001 THEN RAISE; END IF;
  IF SQLCODE != -2292 THEN RAISE_APPLICATION_ERROR(-20002, 'Delete referenced machine: expected ORA-02292, got ' || SQLCODE); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Delete referenced machine rejected with ORA-02292');
END;
/
ROLLBACK TO a12;

SAVEPOINT a13;
DECLARE
  v_name EMPLOYEE.name%TYPE;
BEGIN
  INSERT INTO EMPLOYEE VALUES (9013, 'Acceptance Row', 'Admin', 'Morning', '9000009013', DATE '2026-09-29');
  SELECT name INTO v_name FROM EMPLOYEE WHERE employee_id = 9013;
  IF v_name != 'Acceptance Row' THEN RAISE_APPLICATION_ERROR(-20003, 'Independent insert/search failed'); END IF;
  UPDATE EMPLOYEE SET name = 'Acceptance Updated' WHERE employee_id = 9013;
  DELETE FROM EMPLOYEE WHERE employee_id = 9013;
  IF SQL%ROWCOUNT != 1 THEN RAISE_APPLICATION_ERROR(-20003, 'Independent delete failed'); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Insert/search/update/delete on independent row');
END;
/
ROLLBACK TO a13;

SAVEPOINT a14;
DECLARE
  v_before RAW_MATERIAL.quantity_in_stock%TYPE;
  v_after RAW_MATERIAL.quantity_in_stock%TYPE;
BEGIN
  SELECT quantity_in_stock INTO v_before FROM RAW_MATERIAL WHERE material_id = 1;
  INSERT INTO RESTOCK_LOG VALUES (9014, 1, 1, DATE '2026-09-29', 'Acceptance Supplier', 'AC-9014', 'Acceptance User');
  SELECT quantity_in_stock INTO v_after FROM RAW_MATERIAL WHERE material_id = 1;
  IF v_after != v_before THEN RAISE_APPLICATION_ERROR(-20004, 'Restock changed manual stock'); END IF;
  DBMS_OUTPUT.PUT_LINE('PASS: Restock leaves manual stock unchanged');
END;
/
ROLLBACK TO a14;

PROMPT ALL 14 ACCEPTANCE CHECKS PASSED
