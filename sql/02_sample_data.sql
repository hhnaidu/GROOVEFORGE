WHENEVER SQLERROR EXIT SQL.SQLCODE

INSERT INTO PRESSING_MACHINE VALUES (1, 'Press Alpha', 'GF-120', DATE '2024-01-10', DATE '2026-09-01', 'Operational');
INSERT INTO PRESSING_MACHINE VALUES (2, 'Press Beta', 'GF-220', DATE '2025-02-15', DATE '2026-09-20', 'Under Maintenance');

INSERT INTO RAW_MATERIAL VALUES (1, 'Black Vinyl Pellets', 'PolyVinyl Supply', 800, 'Kilograms', DATE '2026-09-15', 'Raw Store A');
INSERT INTO RAW_MATERIAL VALUES (2, 'Label Paper', 'PrintWorks', 1200, 'Sheets', DATE '2026-09-12', 'Raw Store B');
INSERT INTO RAW_MATERIAL VALUES (3, 'Outer Jacket Cardboard', 'PackRight', 600, 'Pieces', DATE '2026-09-10', 'Raw Store C');

INSERT INTO ARTIST VALUES (1, 'Midnight Atlas', 'Indie Rock', 'Asha Rao', '9000000001', 'asha@example.com', DATE '2026-01-01', DATE '2027-12-31');
INSERT INTO ARTIST VALUES (2, 'Monsoon Echo', 'Ambient', 'Karan Mehta', '9000000002', 'karan@example.com', DATE '2026-02-01', DATE '2027-12-31');

INSERT INTO WAREHOUSE_LOCATION VALUES (1, 'A3', 'A', 'Finished', 2, 500);
INSERT INTO WAREHOUSE_LOCATION VALUES (2, 'Q1', 'Q', 'Quality Hold', 1, 100);

INSERT INTO EMPLOYEE VALUES (1, 'Riya Sen', 'Supervisor', 'Morning', '9000000011', DATE '2025-01-10');
INSERT INTO EMPLOYEE VALUES (2, 'Nisha Patel', 'Warehouse Staff', 'Evening', '9000000012', DATE '2025-03-01');
INSERT INTO EMPLOYEE VALUES (3, 'Arun Das', 'Quality Inspector', 'Morning', '9000000013', DATE '2025-04-15');

INSERT INTO VINYL_RELEASE VALUES (1, 1, 'City After Rain', 'LP', 'Black', 33, 10, 42, NULL);
INSERT INTO VINYL_RELEASE VALUES (2, 2, 'Cloud Patterns', 'EP', 'Blue', 45, 5, 22, NULL);

INSERT INTO MASTER_DISC VALUES (1, 1, 'City Master A', DATE '2026-09-05', 'Riya Sen', 'Master Vault A', 'Good');
INSERT INTO MASTER_DISC VALUES (2, 2, 'Cloud Master A', DATE '2026-09-06', 'Riya Sen', 'Master Vault B', 'New');

UPDATE VINYL_RELEASE SET master_vinyl_disc_id = 1 WHERE release_id = 1;
UPDATE VINYL_RELEASE SET master_vinyl_disc_id = 2 WHERE release_id = 2;

INSERT INTO PRODUCTION_BATCH VALUES (1, 1, 1, 100, TIMESTAMP '2026-09-16 08:00:00', TIMESTAMP '2026-09-16 14:00:00', 'Riya Sen', 'Completed');
INSERT INTO PRODUCTION_BATCH VALUES (2, 2, 1, 50, NULL, NULL, 'Riya Sen', 'Scheduled');
INSERT INTO PRODUCTION_BATCH VALUES (3, 1, 1, 30, TIMESTAMP '2026-09-18 09:00:00', NULL, 'Riya Sen', 'Quality Hold');

INSERT INTO MAINTENANCE_LOG VALUES (1, 1, DATE '2026-09-01', 'Routine service', 'Cleaned and inspected', 'Service Team', DATE '2026-10-01');
INSERT INTO MAINTENANCE_LOG VALUES (2, 2, DATE '2026-09-20', 'Hydraulic leak', 'Seal replaced and tested', 'Service Team', DATE '2026-09-30');

INSERT INTO RESTOCK_LOG VALUES (1, 1, 100, DATE '2026-09-15', 'PolyVinyl Supply', 'PV-001', 'Nisha Patel');
INSERT INTO RESTOCK_LOG VALUES (2, 2, 500, DATE '2026-09-12', 'PrintWorks', 'PW-002', 'Nisha Patel');
INSERT INTO RESTOCK_LOG VALUES (3, 3, 250, DATE '2026-09-10', 'PackRight', 'PR-003', 'Nisha Patel');

INSERT INTO BATCH_MATERIAL_USAGE VALUES (1, 1, 1, 18, 'Kilograms');
INSERT INTO BATCH_MATERIAL_USAGE VALUES (2, 1, 2, 200, 'Sheets');
INSERT INTO BATCH_MATERIAL_USAGE VALUES (3, 1, 3, 100, 'Pieces');

INSERT INTO QUALITY_CHECK VALUES (1, 1, 5, 'Arun Das', TIMESTAMP '2026-09-16 15:00:00', 'None', 'None', 'Pass', 'Sample approved');
INSERT INTO QUALITY_CHECK VALUES (2, 3, 5, 'Arun Das', TIMESTAMP '2026-09-18 12:00:00', 'Warping', 'None', 'Fail', 'Held for review');

INSERT INTO FINISHED_RECORD VALUES (1, 1, 1, 'GF-CAR-0001', 'Standard', 'Mint', 'Warehouse Shelf A3');
INSERT INTO FINISHED_RECORD VALUES (2, 1, 1, 'GF-CAR-0002', 'Standard', 'Mint', 'Warehouse Shelf A3');
INSERT INTO FINISHED_RECORD VALUES (3, 3, 1, 'GF-CAR-0101', 'Standard', 'Good', 'Quality Hold Area');

INSERT INTO CUSTOMER_ORDER VALUES (1, 1, 'Atlas Label', 100, DATE '2026-09-10', 'Ready to Ship', '12 Atlas Lane, Mumbai', NULL, NULL);
INSERT INTO CUSTOMER_ORDER VALUES (2, 2, 'Echo Label', 50, DATE '2026-09-12', 'Pending', '8 Monsoon Road, Pune', NULL, NULL);

COMMIT;
