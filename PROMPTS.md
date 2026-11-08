# \# PROMPTS.md

# 

# Each entry: date, what I asked, what the AI produced, what was wrong, what I did instead (commit link).

# 

# \## 2026-11-08 - Entry 1: Setup did not match my machine

# Asked: ChatGPT for the project setup (Spring Boot, PostgreSQL, React/Vite).

# Produced: Maven config for Java 25, a dependency that does not exist on Maven Central, and a Flyway dependency setup that broke Maven's version management.

# Wrong: my installed JDK was 21, so the build failed. The missing dependency and the Flyway setup also broke the build.

# Did instead: set Java to 21, removed the missing dependency, fixed the Flyway dependency, reset the PostgreSQL app user's password. Checked by running the Maven build.

# Commit: https://github.com/lokesh-v-22/ledgerline/commit/11acb9c

# 

# \## 2026-11-08 - Entry 2: Commands assumed things about my computer

# Asked: Claude for the commands to create the project folders and the database.

# Produced: commands that assumed my Desktop was a normal folder, that psql was available from any folder, and that PostgreSQL was version 17.

# Wrong: my Desktop was redirected into OneDrive, psql was not on my PATH, and the installed version was 18.

# Did instead: put the project in C:\\Projects (outside OneDrive) and ran psql with its full path for version 18.

# Commit: https://github.com/lokesh-v-22/ledgerline/commit/57b3acf

# 

# \## 2026-11-08 - Entry 3: Audit log was not really tamper-proof (audit trail)

# Asked: Claude for a database schema with an append-only audit log and ledger.

# Produced: triggers that block UPDATE and DELETE on the audit, ledger and transaction tables.

# Wrong: the app's database user owned the tables, so it could drop the triggers, and TRUNCATE is not stopped by row triggers. Anyone who took over the app could erase the audit trail. The schema also did not check that each transaction's ledger entries add up to zero.

# Did instead: created a separate owner user (ledgerline\_owner) that only runs migrations, limited the app user to SELECT and INSERT on the audit, ledger and transaction tables, added triggers that block TRUNCATE, and added a deferred check that every transaction's ledger entries sum to zero. Verified: as the app user, DELETE and TRUNCATE on audit\_log give "permission denied" and DROP TRIGGER gives "must be owner".

# Commit: https://github.com/lokesh-v-22/ledgerline/commit/daae8df

