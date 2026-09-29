WHENEVER SQLERROR EXIT SQL.SQLCODE
SET SERVEROUTPUT ON

PROMPT === BEFORE ===
SELECT batch_id, status FROM PRODUCTION_BATCH WHERE batch_id = 1;
SELECT order_id, status FROM CUSTOMER_ORDER WHERE order_id = 1;

SAVEPOINT before_quality_hold;
UPDATE PRODUCTION_BATCH SET status = 'Quality Hold' WHERE batch_id = 1;
UPDATE CUSTOMER_ORDER SET status = 'Quality Hold' WHERE order_id = 1;

PROMPT === DURING (UNCOMMITTED) ===
SELECT batch_id, status FROM PRODUCTION_BATCH WHERE batch_id = 1;
SELECT order_id, status FROM CUSTOMER_ORDER WHERE order_id = 1;

ROLLBACK TO before_quality_hold;

PROMPT === AFTER ROLLBACK ===
SELECT batch_id, status FROM PRODUCTION_BATCH WHERE batch_id = 1;
SELECT order_id, status FROM CUSTOMER_ORDER WHERE order_id = 1;
