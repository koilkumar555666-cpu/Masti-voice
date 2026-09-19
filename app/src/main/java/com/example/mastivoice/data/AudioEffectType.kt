package com.example.mastivoice.data

enum class AudioEffectType(
    val displayName: String,
    val description: String,
    val emoji: String,
    val pitchFactor: Float, // 1.0 is normal
    val speedFactor: Float, // 1.0 is normal
    val isReverse: Boolean = false,
    val isRobot: Boolean = false,
    val isWalkieTalkie: Boolean = false,
    val isMegaphone: Boolean = false,
    val isAlien: Boolean = false,
    val isEcho: Boolean = false,
    val echoDelayMs: Int = 0,
    val echoDecay: Float = 0f
) {
    NORMAL(
        displayName = "Original",
        description = "Natural clean voice",
        emoji = "🎙️",
        pitchFactor = 1.0f,
        speedFactor = 1.0f
    ),
    CHIPMUNK(
        displayName = "Chipmunk",
        description = "Cute squeaky high pitch",
        emoji = "🐿️",
        pitchFactor = 1.55f,
        speedFactor = 1.2f
    ),
    HELIUM(
        displayName = "Helium Gas",
        description = "Hilarious balloon squeak",
        emoji = "🎈",
        pitchFactor = 1.85f,
        speedFactor = 1.3f
    ),
    MONSTER(
        displayName = "Monster",
        description = "Deep scary monster roar",
        emoji = "👹",
        pitchFactor = 0.65f,
        speedFactor = 0.9f
    ),
    GIANT(
        displayName = "Deep Giant",
        description = "Titan sub-bass rumble",
        emoji = "🗿",
        pitchFactor = 0.5f,
        speedFactor = 0.8f
    ),
    ROBOT(
        displayName = "Cyber Robot",
        description = "Metallic ring modulated voice",
        emoji = "🤖",
        pitchFactor = 1.0f,
        speedFactor = 1.0f,
        isRobot = true
    ),
    ALIEN(
        displayName = "Alien Martian",
        description = "Sci-fi vibrato modulation",
        emoji = "👽",
        pitchFactor = 1.25f,
        speedFactor = 1.0f,
        isAlien = true
    ),
    WALKIE_TALKIE(
        displayName = "Walkie Talkie",
        description = "Lo-fi military radio filter",
        emoji = "📻",
        pitchFactor = 1.05f,
        speedFactor = 1.0f,
        isWalkieTalkie = true
    ),
    MEGAPHONE(
        displayName = "Megaphone",
        description = "Loud distorted speaker",
        emoji = "📢",
        pitchFactor = 1.0f,
        speedFactor = 1.0f,
        isMegaphone = true
    ),
    ECHO_CAVE(
        displayName = "Echo Cave",
        description = "Deep cave canyon echoes",
        emoji = "🏔️",
        pitchFactor = 0.95f,
        speedFactor = 1.0f,
        isEcho = true,
        echoDelayMs = 240,
        echoDecay = 0.55f
    ),
    REVERSE(
        displayName = "Reverse Voice",
        description = "Hilarious backwards audio",
        emoji = "⏪",
        pitchFactor = 1.0f,
        speedFactor = 1.0f,
        isReverse = true
    ),
    SLOW_MO(
        displayName = "Slow Motion",
        description = "Cinematic slow deep drag",
        emoji = "🐢",
        pitchFactor = 0.7f,
        speedFactor = 0.65f
    ),
    TURBO(
        displayName = "Turbo Rush",
        description = "Fast-forward high energy",
        emoji = "⚡",
        pitchFactor = 1.35f,
        speedFactor = 1.55f
    ),
    SPOOKY_GHOST(
        displayName = "Spooky Ghost",
        description = "Eerie haunting resonance",
        emoji = "👻",
        pitchFactor = 0.75f,
        speedFactor = 0.85f,
        isEcho = true,
        echoDelayMs = 380,
        echoDecay = 0.65f
    ),
    CUSTOM(
        displayName = "Custom Tuning",
        description = "Fine-tune pitch & tempo",
        emoji = "🎛️",
        pitchFactor = 1.0f,
        speedFactor = 1.0f
    )
}
