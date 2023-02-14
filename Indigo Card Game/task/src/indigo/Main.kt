//package indigo

enum class Suits(val symbol: String) {
    SPADES("♠"),
    HEARD("♥"),
    DIAMONDS("♦"),
    CLUBS("♣")
}

enum class Ranks(val symbol: String) {
    ACE("A"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K")
}

class Card(val rank: Ranks, val suit: Suits) {
    override fun toString() = "${rank.symbol}${suit.symbol}"
}

enum class Deck(var cards: MutableList<Card>) {
    GAME_DECK(mutableListOf()),
    TABLE_DECK(mutableListOf())
}

enum class Players(var value: Player) {
    PLAYER(Player("Player")),
    COMPUTER(Player("Computer"))
}

class Player(val name: String) {
    var firstTurn: Boolean = false
    var turn: Boolean = false
    var deck: MutableList<Card> = mutableListOf()
    var score: Int = 0
    var winsCards: MutableList<Card> = mutableListOf()
}

fun main() {
    // Start the game
    startGame()

    // Main game
    game()
}

private fun startGame() {
    println("Indigo Card Game")

//    Ask questions until we find out who goes first
    while (!Players.PLAYER.value.turn && !Players.COMPUTER.value.turn) {
        println("Play first?")
        when (readln().lowercase()) {
            "yes" -> {
                Players.PLAYER.value.firstTurn = true
                Players.PLAYER.value.turn = true
            }
            "no" -> {
                Players.COMPUTER.value.firstTurn = true
                Players.COMPUTER.value.turn = true
            }
        }
    }

//    Let's get(create) a deck and deal cards on table
    Suits.values().forEach { suit ->
        Ranks.values().forEach { rank ->
            Deck.GAME_DECK.cards.add(Card(rank, suit))
        }
    }
    Deck.GAME_DECK.cards.shuffle()

    Deck.TABLE_DECK.cards = Deck.GAME_DECK.cards.subList(0, 4)
    println("Initial cards on the table: ${Deck.TABLE_DECK.cards.joinToString(" ")}")

    // TODO() FIX crutch for deleting a value  -  fix this!
    val temp = Deck.GAME_DECK.cards.filter { it !in Deck.TABLE_DECK.cards }.toMutableList()
    Deck.GAME_DECK.cards = temp

//    Hand out cards to players
    dealCards()
}

private fun game() {
    fun messageText() = if (Deck.TABLE_DECK.cards.isEmpty())
        println("No cards on the table")
    else
        println("\n${Deck.TABLE_DECK.cards.size} cards on the table, and the top card is ${Deck.TABLE_DECK.cards.last()}")

//    Game continues while players have a turn
    while (Players.PLAYER.value.turn || Players.COMPUTER.value.turn) {
        messageText()

        if (Players.PLAYER.value.deck.isEmpty() && Players.COMPUTER.value.deck.isEmpty()) dealCards()

        when {
            Players.PLAYER.value.turn -> {
                val cards =
                    Players.PLAYER.value.deck.mapIndexed { index, card -> "${index + 1})$card" }
                        .joinToString(" ")
                println("Cards in hand: $cards")
                move(Players.PLAYER)
            }
            Players.COMPUTER.value.turn -> {
                move(Players.COMPUTER)
            }
        }

        if (
            Deck.GAME_DECK.cards.isEmpty() &&
            Deck.TABLE_DECK.cards.size == 52 &&
            Players.PLAYER.value.deck.isEmpty() &&
            Players.COMPUTER.value.deck.isEmpty()
        ) {
            messageText()
            gameOver()
        }
    }
}


private fun dealCards() {
    // Take turns dealing out cards
    for (index in 0..11) {
        if (index % 2 == 0) {
            Players.COMPUTER.value.deck.add(Deck.GAME_DECK.cards[index])
        } else {
            Players.PLAYER.value.deck.add(Deck.GAME_DECK.cards[index])
        }
    }

    // Remove cards from deck
    Deck.GAME_DECK.cards.removeAll(Players.COMPUTER.value.deck)
    Deck.GAME_DECK.cards.removeAll(Players.PLAYER.value.deck)
}

private fun move(currentPlayer: Players) {
    val currentPlayerValues = currentPlayer.value
    val tableCards = Deck.TABLE_DECK.cards
    val topCardOnTable = tableCards.last()

    lateinit var playingCard: Card

    fun putCard(playingCard: Card) {

        // If current player win
        if (playingCard.rank == topCardOnTable.rank || playingCard.suit == topCardOnTable.suit) {
            println("${currentPlayerValues.name} wins cards")

            // Update scores current winner
            currentPlayerValues.score += 1

            // Update winners deck card current winner
            currentPlayerValues.winsCards.addAll(tableCards + playingCard)
            tableCards.clear()

            // Print statistic
            println("Score: Player ${Players.PLAYER.value.score} - Computer ${Players.COMPUTER.value.score}")
            println("Cards: Player ${Players.PLAYER.value.winsCards.size} - Computer ${Players.COMPUTER.value.winsCards.size}")
        } else {
            // Put card on table
            tableCards.add(playingCard)
        }

        // Remove playing card from deck current player
        currentPlayerValues.deck.remove(playingCard)

        // Change players turn
        Players.PLAYER.value.turn = true
        Players.COMPUTER.value.turn = true
        currentPlayerValues.turn = false
    }

    when (currentPlayer) {
        // when user move
        Players.PLAYER -> {
            // output cards in hand user
            println("Choose a card to play (1-${currentPlayerValues.deck.size}):")

            // received player choice
            when (val choice = readln()) {
                in (1..currentPlayerValues.deck.size).map { it.toString() } -> {
                    playingCard = currentPlayerValues.deck[choice.toInt() - 1]
                    putCard(playingCard)
                }
                "exit" -> {
                    gameOver()
                }
                else -> move(currentPlayer)
            }
        }
        // when computer move
        Players.COMPUTER -> {
//            playingCard = currentPlayerValues.deck.first() // TODO REMOVE THIS
            val candidateCards: List<Card>
            var cardsSameSuits = listOf<Card>()
            var cardsSameRanks = listOf<Card>()

            if (currentPlayerValues.deck.filter { card ->
                    card.rank == tableCards.last().rank ||
                    card.suit == tableCards.last().suit
                }.isEmpty()
            ) {
                candidateCards = currentPlayerValues.deck
            } else {
                candidateCards = currentPlayerValues.deck.filter { card ->
                    card.rank != tableCards.last().rank ||
                    card.suit != tableCards.last().suit
                }

                if (candidateCards.groupBy { it.suit }.filter { it.value.size > 1 }.values.reduce { acc, cards -> acc + cards }.isNotEmpty())
                    cardsSameSuits = candidateCards.groupBy { it.suit }.filter { it.value.size > 1 }.values.reduce { acc, cards -> acc + cards }

                if (candidateCards.groupBy { it.rank }.filter { it.value.size > 1 }.values.reduce { acc, cards -> acc + cards }.isNotEmpty())
                    cardsSameRanks = candidateCards.groupBy { it.rank }.filter { it.value.size > 1 }.values.reduce { acc, cards -> acc + cards }
            }

            // for steps 3 and 4
            playingCard = when {
                // 3) If there are no cards on the table
                // 4) If there are cards on the table but no candidate cards, use the same tactics as in step 3
                tableCards.isEmpty() -> when {
                    // If there are cards in hand with the same suit, throw one of them at random
                    cardsSameSuits.isNotEmpty() -> cardsSameSuits.random()
                    // If there are no cards in hand with the same suit, but there are cards with the same rank, then throw one of them at random
                    cardsSameRanks.isNotEmpty() -> cardsSameRanks.random()
                    // If there are no cards in hand with the same suit or rank, throw any card at random.
                    else -> currentPlayerValues.deck.random()
                }

                // 1) If there is only one card in hand, put it on the table
                currentPlayerValues.deck.size == 1 -> currentPlayerValues.deck.first()

                // 2) If there is only one candidate card, put it on the table
                candidateCards.size == 1 -> candidateCards.first()

                // 5) If there are two or more candidate cards
                else -> {
                    when {
                        // If there are 2 or more candidate cards with the same suit as the top card on the table, throw one of them at random
                        cardsSameSuits.size >= 2 -> cardsSameSuits.random()
                        // If the above isn't applicable, but there are 2 or more candidate cards with the same rank as the top card on the table, throw one of them at random
                        cardsSameRanks.size >= 2 -> cardsSameRanks.random()
                        // If nothing of the above is applicable, then throw any of the candidate cards at random.
                        else -> candidateCards.random()
                    }
                }
            }

            println("Computer plays $playingCard")
            putCard(playingCard)
        }
    }
}

fun gameOver() {
//    Turn off options to move for player and computer
    Players.PLAYER.value.turn = false
    Players.COMPUTER.value.turn = false

    println("Game Over")
}


