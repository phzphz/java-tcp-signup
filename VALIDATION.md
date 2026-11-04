# Publication validation

Checked on 2026-11-04. These checks describe the curated publication copy, not an original grade or submission result.

Compiled with Java 17 and ran the actual TCP server/client. Twelve concurrent join requests against a five-person list produced exactly five successes and seven capacity rejections. Checked listing, totals and invalid list handling. This is a small local concurrency check, not a load/soak test.
