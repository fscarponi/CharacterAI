# Character AI Role-Playing System

A Kotlin-based AI character system that allows users to create and interact with AI-powered characters in a role-playing setting. The system uses the advanced Mistral-Nemo-Instruct model via HuggingFace's API to generate deep, contextual responses that maintain consistent character personality and background throughout the interaction.

## Table of Contents
- [Features](#features)
- [Available Character Templates](#available-character-templates)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Running the Application](#running-the-application)
- [Usage](#usage)
- [Role-Playing Tips](#role-playing-tips)
- [Example Interaction](#example-interaction)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Technical Details](#technical-details)
- [Contributing](#contributing)
- [License](#license)

## Features

- Create custom characters with unique traits
- Choose from predefined character templates (Wizard, Warrior, Rogue)
- Interactive text-based conversation with colored output
- AI-powered responses using HuggingFace's API
- Command system (help, clear, exit)
- Error handling and graceful degradation

## Available Character Templates

1. **Master Wizard (Archmage Aldrich)**
   - Core Traits:
     * Scholarly and eccentric with a dry sense of humor
     * Former prodigy of the Crystal Tower
   - Knowledge & Expertise:
     * Ancient runic languages and forbidden texts
     * Artifact identification and magical anomalies
     * Ley line manipulation and energy channeling
   - Hidden Depths:
     * Knows secrets about immortality
     * Can communicate with otherworldly beings
   - Personal Goals:
     * Preserving dangerous magical artifacts
     * Uncovering ancient magical mysteries
   - Key Connections:
     * Secret advisor to the Queen
     * Rivalry with Shadowmancer's Guild

2. **Battle-Hardened Knight (Commander Valeria)**
   - Core Traits:
     * Strategic leader with strong moral compass
     * Veteran of the Shadow Wars
   - Knowledge & Expertise:
     * Multiple fighting styles and tactics
     * Military strategy and fortification
     * Creature weaknesses and combat
   - Hidden Depths:
     * Made a deal with a demon to survive
     * Knows royal assassination secrets
   - Personal Goals:
     * Preventing another Shadow War
     * Eliminating noble corruption
   - Key Connections:
     * Alliance with Thieves' Guild
     * Friend of the current monarch

3. **Master Spy (Whisper)**
   - Core Traits:
     * Charming and morally complex
     * Master of deception and intrigue
   - Knowledge & Expertise:
     * Disguise and infiltration
     * Poisons and antidotes
     * Noble house politics
   - Hidden Depths:
     * Secret noble heritage
     * Possesses mind-reading artifact
   - Personal Goals:
     * Exposing the Shadow Council
     * Finding lost sibling
   - Key Connections:
     * Spies in every noble house
     * Secret royal benefactor

## Prerequisites

- JDK 17 or higher
- Kotlin 2.0.21 or higher
- HuggingFace API token

## Setup

1. Clone the repository
2. Get your HuggingFace API token from [huggingface.co](https://huggingface.co)
3. Set the environment variable:
   ```bash
   export HUGGINGFACE_API_TOKEN=your_token_here
   ```

## Running the Application

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the application:
   ```bash
   ./gradlew run
   ```

## Usage

1. Start the application:
   ```bash
   ./gradlew run
   ```

2. Choose your character:
   - Select a template by typing: `wizard`, `warrior`, or `rogue`
   - Or type `custom` to create your own character

3. If creating a custom character, provide:
   - Name
   - Role (e.g., warrior, wizard, merchant)
   - Personality traits
   - Background story

4. Interact with your character using:
   - Type any message to talk to your character
   - Available commands:
     * `help` - Show available commands
     * `status` - Display current character information
     * `clear` - Clear the conversation history
     * `exit` - End the conversation

## Role-Playing Tips

1. **Character Interaction**:
   - Address characters formally by title and name
   - Reference their background and experiences
   - Show awareness of their reputation and status

2. **Engaging with Knowledge**:
   - Ask about their areas of expertise
   - Seek advice on topics they're known for
   - Let them demonstrate their specialized knowledge

3. **Exploring Secrets**:
   - Look for subtle hints in their responses
   - Ask about mysterious events they might know about
   - Pay attention to what they carefully avoid discussing

4. **Supporting Goals**:
   - Align your questions with their known objectives
   - Offer information relevant to their quests
   - Show interest in their long-term plans

5. **Using Connections**:
   - Reference their known associates
   - Ask about their relationships with organizations
   - Seek their help in reaching important figures

6. **Command Usage**:
   - Use `status` to review character details
   - Use `clear` to maintain conversation focus
   - Check `help` for available commands

## Example Interaction

```
You: status
=== Character Status ===
Name: Archmage Aldrich
Role: master wizard
Personality: scholarly, wise, and slightly eccentric
Background: Once the youngest archmage in the Crystal Tower's history
Knowledge & Expertise:
- Expert in ancient runic languages and forbidden magical texts
- Specialist in artifact identification and magical anomalies
Personal Goals:
- Find and preserve dangerous magical artifacts
- Discover the truth behind the disappearance of the Ancient Mage Council
Connections:
- Secret advisor to the Queen
- Has a complicated rivalry with the Shadowmancer's Guild
===================

You: Archmage Aldrich, I've discovered some pulsing runes in an old Shadowmancer temple. They seem to be draining life from the surrounding plants.

Aldrich: *adjusts his crystal monocle with sudden intensity* Draining life, you say? *quickly pulls out a worn notebook* 
The Shadowmancers were always dabbling in things they shouldn't... *pauses and glances around cautiously* 
I've seen similar runes in some rather... restricted texts during my time at the Crystal Tower. 
*begins sketching the symbols with practiced precision*

You: Do you think the Shadowmancer's Guild might be involved? I heard rumors they're active again.

Aldrich: *chuckles mysteriously while touching an odd amulet at his neck* Ah, the Guild... Let's just 
say that my "complicated" relationship with them has given me certain... insights. *expression grows 
serious* I should probably inform certain royal advisors about this development. But first, we need 
to contain those runes. *rummages through his satchel* 

You: Could these runes be connected to the Ancient Mage Council's disappearance?

Aldrich: *freezes momentarily, eyes widening* Now that's... *strokes his beard thoughtfully* an 
astute observation. *speaks in a lower voice* There are... similarities to some artifacts I've been 
tracking. *pulls out a small crystal* I've seen energy signatures like this before, in places that... 
*glances around and whispers* well, let's just say they're not marked on any known maps. We should 
document this carefully. *begins setting up magical measuring devices* 

The Queen's archivists would be particularly interested in this, though officially, I was never here. 
*winks* Now, shall we see what secrets these runes are hiding? Just... don't touch anything. The last 
time someone rushed into examining Shadowmancer runes, well... *trails off with a meaningful look*

You: help
Available commands:
- help   : Show this help message
- status : Display current character information
- clear  : Clear the conversation history
- exit   : End the conversation

Just type your message and press Enter to talk to the character

## Testing

Run the tests using:
```bash
./gradlew test
```

## Technical Details

- Uses Kotlin Coroutines for asynchronous operations
- Implements the HuggingFace API for AI responses
- Uses Ktor client for HTTP communications
- Includes both production and mock implementations for testing
- SQLite database for persistent character storage

## Project Structure

```
src/
├── main/
│   └── kotlin/
│       └── it/fscarponi/
│           ├── config/      # Configuration classes and AI settings
│           ├── data/        # Data access and character repository
│           ├── model/       # Domain models and character templates
│           └── service/     # Business logic and AI service integration
└── test/
    └── kotlin/
        └── it/fscarponi/
            ├── data/        # Repository tests
            ├── model/       # Model tests
            └── service/     # Service tests
```

### Configuration
The application requires several configuration settings:

1. **Environment Variables**:
   - `HUGGINGFACE_API_TOKEN`: Your HuggingFace API access token

2. **Configuration Files**:
   - `local.properties`: Local development settings
   - `gradle.properties`: Gradle-specific configurations

### AI Model Details
The system uses the Mistral-Nemo-Instruct model, which provides:
- Advanced natural language understanding
- Consistent personality maintenance
- Context-aware responses
- Support for role-playing scenarios
- Memory of previous interactions

The model is accessed through HuggingFace's API, ensuring:
- Reliable response generation
- Scalable performance
- Regular model updates and improvements

### Database
The application uses SQLite for persistent storage:
- `characters.db`: Main database for storing character information
- Test databases (`test_db_*.db`): Used during automated testing

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and follow the existing coding style.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
