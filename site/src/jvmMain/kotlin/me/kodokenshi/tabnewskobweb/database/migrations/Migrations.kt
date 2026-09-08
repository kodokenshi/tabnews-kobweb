package me.kodokenshi.tabnewskobweb.database.migrations

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.security.MessageDigest

class Migrations(
	private val driver: String,
	private val databaseURL: String,
	private val databaseUser: String,
	private val databasePassword: String,
	private val migrationsPath: String
) {
	
	private val log = StringBuilder()
	private val localMigrations = mutableListOf<MigrationFile>()
	private val migratedMigrations = mutableListOf<MigrationFile>()
	
	private fun findFiles() {
		
		val dir = File(migrationsPath)
		if (!dir.exists()) dir.mkdirs()
		
		val files = dir.listFiles {
			it.extension.equals("sql", true) && it.name.count { c -> c == '_' } == 2
		}.orEmpty().mapNotNull { file ->
			
			val fileName = file.nameWithoutExtension.split("_")
			val script = file.readText()
			
			MigrationFile(
				version = fileName.first().toLongOrNull() ?: return@mapNotNull null,
				author = fileName.getOrNull(1) ?: return@mapNotNull null,
				description = fileName.getOrNull(2) ?: return@mapNotNull null,
				script = script,
				checksum = sha256(script)
			)
			
		}.sortedBy { it.version }
		
		localMigrations.addAll(files)
		
	}
	
	private fun sha256(input: String): String {
		val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
		return bytes.joinToString("") { "%02x".format(it) }
	}
	
	init { findFiles() }
	
	fun getLog() = log.toString()
	fun getMigratedMigrations() = migratedMigrations.toList()
	
	fun findPendingMigrations(): List<MigrationFile> {
		
		if (localMigrations.isEmpty()) return emptyList()
		
		val database = Database.connect(
			url = databaseURL,
			user = databaseUser,
			password = databasePassword,
			driver = driver,
		)
		
		return transaction(database) {
			
			SchemaUtils.create(MigrationHistoryTable)
			
			val appliedMigrations = MigrationHistoryTable.selectAll().map { it[MigrationHistoryTable.version] }
			localMigrations.filter { it.version !in appliedMigrations }
			
		}
		
	}
	fun migrate(dryRun: Boolean = false) {
		
		migratedMigrations.clear()
		val prefix = "[MIGRATIONS]${if (dryRun) " [DRY-RUN]" else ""}"
		
		"$prefix Initiating migrations...".also {
			log.append(it)
			println(it)
		}
		
		if (localMigrations.isEmpty()) {
			"$prefix There's no local migration file. Migrations aborted.".also {
				log.append("\n$it")
				println(it)
			}
			return
		}
		
		val database = Database.connect(
			url = databaseURL,
			user = databaseUser,
			password = databasePassword,
			driver = driver,
		)
		
		transaction(database) {
			
			SchemaUtils.create(MigrationHistoryTable)
			
			val migratedMigrations = MigrationHistoryTable.selectAll().associate {
				it[MigrationHistoryTable.version] to it[MigrationHistoryTable.checksum]
			}
			var migrated = 0
			localMigrations.forEach { migrationFile ->
				
				val migrationName = "'${migrationFile.version}' '${migrationFile.description.ifBlank { "undescribed" }}' by '${migrationFile.author}'"
				
				val migratedChecksum = migratedMigrations[migrationFile.version]
				if (migratedChecksum != null) {
					
					if (migratedChecksum != migrationFile.checksum) {
						"$prefix [WARN] Migration $migrationName is different from the migrated on database!".also {
							log.append("\n$it")
							println(it)
						}
						throw IllegalStateException("Migration $migrationName is different from the migrated on database!")
					}
					return@forEach
					
				}
				
				if (migrationFile.script.isBlank()) {
					"$prefix [WARN] Migration $migrationName has no script!".also {
						log.append("\n$it")
						println(it)
					}
					throw IllegalStateException("Migration $migrationName has no script!")
				}
				
				if (dryRun) "$prefix Migration $migrationName script:\n${migrationFile.script}".also {
					log.append("\n$it")
					println(it)
				} else {
					
					"$prefix Migrating migration: $migrationName".also {
						log.append("\n$it")
						println(it)
					}
					
					exec(migrationFile.script)
					
					MigrationHistoryTable.insert {
						it[version] = migrationFile.version
						it[author] = migrationFile.author
						it[description] = migrationFile.description
						it[script] = migrationFile.script
						it[checksum] = migrationFile.checksum
						it[installedOn] = System.currentTimeMillis()
					}
					
					this@Migrations.migratedMigrations.add(migrationFile)
					
				}
				
				migrated++
				
			}
			
			if (migrated == 0) {
				"$prefix Complete. *Everything is up-to-date.*".also {
					log.append("\n$it")
					println(it)
				}
				return@transaction
			}
			
			if (dryRun) {
				
				rollback()
				"$prefix Complete. *No changes were saved.*".also {
					log.append("\n$it")
					println(it)
				}
				
			} else "$prefix Complete. *Changes have been saved.*".also {
				log.append("\n$it")
				println(it)
			}
			
		}
		
	}
	
}