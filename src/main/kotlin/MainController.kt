import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.netflix.hystrix.exception.HystrixRuntimeException
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.Controller
import tornadofx.getValue
import tornadofx.setValue

class MainController : Controller() {
    val model = Model.load()
    val keyStroker = KeyStroker()
    var twitchClient: TwitchClient? = null

    val eventConsole = EventConsole()

    val channelNameProperty = SimpleStringProperty(model.channelName)
    private var channelName by channelNameProperty

    val oauthTokenProperty = SimpleStringProperty(model.oauthToken)
    private var oauthToken by oauthTokenProperty

    val startedProperty = SimpleBooleanProperty(false)
    private var started by startedProperty

    val errorTextProperty = SimpleStringProperty("")
    private var errorText by errorTextProperty

    fun start() {
        errorText = ""

        val credential = OAuth2Credential(null, oauthToken)
        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withEnablePubSub(true)
            .withDefaultAuthToken(credential)
            .build()

        val channelId = try {
            val resultList = twitchClient!!.helix.getUsers(oauthToken, null, listOf(channelName)).execute()
            resultList.users.find { it.displayName == channelName }!!.id
        } catch (e: HystrixRuntimeException) {
            errorText = "Channel not found"
            eventConsole.log("Error starting: $errorText")
            return
        } catch (e: NullPointerException) {
            errorText = "Channel not found"
            eventConsole.log("Error starting: $errorText")
            return
        }

        // Strip "oauth:" from entered string to show the user that it happened
        oauthToken = oauthToken

        // Save the channelName and oauthToken
        model.channelName = channelName
        model.oauthToken = oauthToken
        model.save()

        twitchClient?.eventManager?.getEventHandler(SimpleEventHandler::class.java)?.registerListener(this)

        twitchClient?.clientHelper?.enableFollowEventListener(channelName)
        twitchClient?.pubSub?.listenForChannelPointsRedemptionEvents(credential, channelId)
        twitchClient?.pubSub?.listenForCheerEvents(credential, channelId)
        twitchClient?.pubSub?.listenForSubscriptionEvents(credential, channelId)

        started = true
        eventConsole.log("Started!")
    }

    fun <T : MetaShortcut> getShortcutsList(): ObservableList<T> {
        return model.followShortcuts as ObservableList<T>
    }

    fun <T> addShortcut(clazz: Class<T>, value: String, shortcutOnEvent: Shortcut, waitTime: Long, shortcutAfterWait: Shortcut, alwaysFire: Boolean) {
        when (clazz) {
            FollowShortcut::class.java -> model.followShortcuts.add(FollowShortcut(shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire))
            ChannelPointsShortcut::class.java -> model.channelPointsShortcuts.add(ChannelPointsShortcut(value, shortcutOnEvent, waitTime, shortcutAfterWait, alwaysFire))
        }

        model.save()
    }

    fun <T : MetaShortcut> removeShortcut(shortcut: MetaShortcut) {

    }

//    @EventSubscriber
//    fun handleFollow(event: FollowEvent) {
//        eventConsole.log("Follow Event - User: " + event.user.name)
//        model.followShortcuts.forEach {
//            keyStroker.strokeKeys(it.modifiers, it.key)
//            eventConsole.log("Shortcut fired: " + it.shortcutString)
//        }
//    }
//
//    @EventSubscriber
//    fun handleChannelPointsRedemption(event: ChannelPointsRedemptionEvent) {
//        val title = event.redemption.reward.title
//        eventConsole.log("Channel Points Redemption Event - User: " + event.redemption.user.displayName + ", Title: $title")
//        model.channelPointsShortcuts.filter { it.title == title }.forEach {
//            keyStroker.strokeKeys(it.modifiers, it.key)
//            eventConsole.log("Shortcut fired: " + it.shortcutString)
//        }
//    }
//
//    @EventSubscriber
//    fun handleCheer(event: CheerEvent) {
//        eventConsole.log("Cheer Event - User: " + event.user.name + ", Bits: " + event.bits)
//        var previous: Shortcut? = null
//        model.cheerShortcuts.forEach {
//            if (event.bits < it.bits) {
//                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//                return
//            }
//            previous = it
//        }
//
//        // The value is higher than the last value in the list
//        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//    }
//
//    @EventSubscriber
//    fun handleSubscription(event: SubscriptionEvent) {
//        if (event.gifted) {
//            return // Handle these elsewhere
//        }
//        eventConsole.log("Subscription Event - User: " + event.user.name + ", Months: " + event.months)
//        var previous: Shortcut? = null
//        model.subscriptionShortcuts.forEach {
//            if (event.months < it.months) {
//                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//                return
//            }
//            previous = it
//        }
//
//        // The value is higher than the last value in the list
//        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//    }
//
//    @EventSubscriber
//    fun handleGiftSubscriptions(event: GiftSubscriptionsEvent) {
//        eventConsole.log("Gift Subscriptions Event - User: " + event.user.name + ", Count: " + event.count)
//        var previous: Shortcut? = null
//        model.giftSubscriptionShortcuts.forEach {
//            if (event.count < it.count) {
//                keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//                return
//            }
//            previous = it
//        }
//
//        // The value is higher than the last value in the list
//        keyStroker.strokeKeys(previous?.modifiers ?: return, previous?.key ?: return)
//    }
}