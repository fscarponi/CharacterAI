package org.example.data

import org.example.model.Character
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

interface CharacterRepository {
    suspend fun initialize()
    suspend fun getAllCharacters(): List<Character>
    suspend fun addCharacter(character: Character)
    suspend fun getCharacterByName(name: String): Character?
    suspend fun deleteCharacter(name: String)
    suspend fun deleteAllCharacters()
    suspend fun cleanup()
}

object Characters : Table() {
    val name = varchar("name", 255)
    val role = varchar("role", 255)
    val personality = text("personality")
    val background = text("background")
    val knowledge = text("knowledge")
    val secrets = text("secrets")
    val goals = text("goals")
    val connections = text("connections")

    override val primaryKey = PrimaryKey(name)
}

class SQLiteCharacterRepository(private val dbPath: String = "characters.db") : CharacterRepository {
    private val json = Json { prettyPrint = true }
    private val database: Database

    init {
        database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC",
        )
    }

    init {
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Characters)
        }
    }

    private fun List<String>.toJson(): String = Json.encodeToString(this)
    private fun String.fromJson(): List<String> = Json.decodeFromString(this)

    private fun ResultRow.toCharacter(): Character = Character(
        name = this[Characters.name],
        role = this[Characters.role],
        personality = this[Characters.personality],
        background = this[Characters.background],
        knowledge = this[Characters.knowledge].fromJson(),
        secrets = this[Characters.secrets].fromJson(),
        goals = this[Characters.goals].fromJson(),
        connections = this[Characters.connections].fromJson()
    )

    override suspend fun initialize() {
        transaction(database) {
            if (Characters.selectAll().count() == 0L) {
                // Add pregenerated characters
                val pregeneratedCharacters = listOf(
                    Character(
                        name = "Eldric Shadowweaver",
                        role = "shadow mage",
                        personality = "mysterious and calculating, with a dry wit and a tendency to speak in riddles about the nature of darkness",
                        background = "A former court mage who discovered forbidden shadow magic and now walks a fine line between light and dark.",
                        knowledge = listOf(
                            "Master of shadow manipulation and illusion",
                            "Expert in forbidden magical texts",
                            "Scholar of ancient dark artifacts",
                            "Specialist in magical concealment",
                            "Understanding of planar boundaries"
                        ),
                        secrets = listOf(
                            "Can see through the eyes of shadows",
                            "Made a pact with an ancient shadow dragon",
                            "Knows the true nature of the Void",
                            "Has visited the Shadow Realm and returned"
                        ),
                        goals = listOf(
                            "Understand the true nature of shadow magic",
                            "Find a way to harness darkness without corruption",
                            "Protect the world from shadow realm invasions",
                            "Train worthy apprentices in responsible shadow magic"
                        ),
                        connections = listOf(
                            "Member of the Shadow Conclave",
                            "Secret informant to the Light Keepers",
                            "Mentor to promising shadow mages",
                            "Ally to certain shadow realm entities"
                        )
                    ),
                    Character(
                        name = "Luna Starweaver",
                        role = "celestial oracle",
                        personality = "serene and ethereal, with an otherworldly presence and profound insight into cosmic matters",
                        background = "Born under a convergence of celestial bodies, she was chosen by the stars themselves to interpret their messages.",
                        knowledge = listOf(
                            "Expert in celestial alignments and prophecy",
                            "Master of astral magic and divination",
                            "Scholar of cosmic entities and phenomena",
                            "Keeper of ancient star charts",
                            "Interpreter of celestial omens"
                        ),
                        secrets = listOf(
                            "Can communicate directly with celestial beings",
                            "Knows the true date of the world's end",
                            "Has seen alternate timelines",
                            "Possesses a fragment of a fallen star"
                        ),
                        goals = listOf(
                            "Prevent a prophesied celestial catastrophe",
                            "Guide others through coming cosmic changes",
                            "Maintain balance between celestial forces",
                            "Preserve ancient stellar knowledge"
                        ),
                        connections = listOf(
                            "High Priestess of the Stellar Temple",
                            "Advisor to multiple royal courts",
                            "Contact with celestial beings",
                            "Member of the Astral Conclave"
                        )
                    ),
                    Character(
                        name = "Thorne Ironheart",
                        role = "dwarven runesmith",
                        personality = "gruff but passionate, with endless enthusiasm for his craft and a deep respect for ancient traditions",
                        background = "Last heir to the legendary Ironheart forge, keeper of secrets passed down through generations of master runesmiths.",
                        knowledge = listOf(
                            "Master of ancient dwarven runes",
                            "Expert in magical metallurgy",
                            "Scholar of forgotten forge techniques",
                            "Specialist in enchanted weaponry",
                            "Knowledge of earth elementals"
                        ),
                        secrets = listOf(
                            "Knows the location of the First Forge",
                            "Can speak the true names of metals",
                            "Possesses the last Soulforge hammer",
                            "Has forged weapons for gods"
                        ),
                        goals = listOf(
                            "Restore the lost art of soulforging",
                            "Find worthy apprentices to pass on knowledge",
                            "Recover lost dwarven artifacts",
                            "Protect the secrets of runesmith craft"
                        ),
                        connections = listOf(
                            "Head of the Runesmith's Guild",
                            "Friend to mountain kings",
                            "Supplier to legendary heroes",
                            "Guardian of ancient forge spirits"
                        )
                    )
                )
                pregeneratedCharacters.forEach { character -> 
                    Characters.insert {
                        it[name] = character.name
                        it[role] = character.role
                        it[personality] = character.personality
                        it[background] = character.background
                        it[knowledge] = character.knowledge.toJson()
                        it[secrets] = character.secrets.toJson()
                        it[goals] = character.goals.toJson()
                        it[connections] = character.connections.toJson()
                    }
                }
            }
        }
    }

    override suspend fun getAllCharacters(): List<Character> {
        return transaction(database) {
            Characters.selectAll().map { it.toCharacter() }
        }
    }

    override suspend fun addCharacter(character: Character) {
        transaction(database) {
            val existing = Characters.select { Characters.name eq character.name }.singleOrNull()
            if (existing != null) {
                throw IllegalArgumentException("Character with name ${character.name} already exists")
            }

            Characters.insert {
                it[name] = character.name
                it[role] = character.role
                it[personality] = character.personality
                it[background] = character.background
                it[knowledge] = character.knowledge.toJson()
                it[secrets] = character.secrets.toJson()
                it[goals] = character.goals.toJson()
                it[connections] = character.connections.toJson()
            }
        }
    }

    override suspend fun getCharacterByName(name: String): Character? {
        return transaction(database) {
            Characters.select { Characters.name eq name }
                .map { it.toCharacter() }
                .singleOrNull()
        }
    }

    override suspend fun deleteCharacter(name: String) {
        transaction(database) {
            Characters.deleteWhere { Characters.name eq name }
        }
    }

    override suspend fun deleteAllCharacters() {
        transaction(database) {
            Characters.deleteAll()
        }
    }

    override suspend fun cleanup() {
        transaction(database) {
            SchemaUtils.drop(Characters)
        }
    }
}
