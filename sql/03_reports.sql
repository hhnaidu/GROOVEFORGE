WHENEVER SQLERROR EXIT SQL.SQLCODE

-- 1. Release catalog
SELECT r.release_id, a.artist_name, r.album_title, r.format, r.color_variant
FROM VINYL_RELEASE r JOIN ARTIST a ON a.artist_id = r.artist_id
ORDER BY r.release_id;

-- 2. Batch tracking
SELECT b.batch_id, r.album_title, m.machine_name, b.status, b.batch_size
FROM PRODUCTION_BATCH b
JOIN VINYL_RELEASE r ON r.release_id = b.release_id
JOIN PRESSING_MACHINE m ON m.machine_id = b.machine_id
ORDER BY b.batch_id;

-- 3. Quality history
SELECT q.check_id, r.album_title, q.batch_id, q.overall_result, q.notes
FROM QUALITY_CHECK q
JOIN PRODUCTION_BATCH b ON b.batch_id = q.batch_id
JOIN VINYL_RELEASE r ON r.release_id = b.release_id
ORDER BY q.check_id;

-- 4. Material usage
SELECT u.usage_id, u.batch_id, m.material_name, u.quantity_used, u.unit
FROM BATCH_MATERIAL_USAGE u JOIN RAW_MATERIAL m ON m.material_id = u.material_id
ORDER BY u.usage_id;

-- 5. Orders awaiting dispatch
SELECT order_id, customer_name, release_id, status
FROM CUSTOMER_ORDER
WHERE status IN ('Pending', 'In Production', 'Quality Hold', 'Ready to Ship')
ORDER BY order_id;

-- 6. Record traceability
SELECT f.serial_number, r.album_title, b.batch_id, m.machine_name, f.current_location
FROM FINISHED_RECORD f
JOIN PRODUCTION_BATCH b ON b.batch_id = f.batch_id
JOIN VINYL_RELEASE r ON r.release_id = f.release_id
JOIN PRESSING_MACHINE m ON m.machine_id = b.machine_id
ORDER BY f.record_id;

-- 7. Maintenance follow-up
SELECT m.machine_name, l.maintenance_date, l.issue_reported, l.next_due_date
FROM MAINTENANCE_LOG l JOIN PRESSING_MACHINE m ON m.machine_id = l.machine_id
ORDER BY l.log_id;

-- 8. Selected master discs
SELECT r.release_id, r.album_title, d.disc_name, d.disc_condition
FROM VINYL_RELEASE r
LEFT JOIN MASTER_DISC d ON d.disc_id = r.master_vinyl_disc_id AND d.release_id = r.release_id
ORDER BY r.release_id;

-- 9. Releases without batches
SELECT r.release_id, r.album_title
FROM VINYL_RELEASE r
WHERE NOT EXISTS (SELECT 1 FROM PRODUCTION_BATCH b WHERE b.release_id = r.release_id)
ORDER BY r.release_id;

-- 10. Failed quality checks
SELECT check_id, batch_id, visual_defects_found, audio_defects_found, notes
FROM QUALITY_CHECK
WHERE overall_result = 'Fail'
ORDER BY check_id;
