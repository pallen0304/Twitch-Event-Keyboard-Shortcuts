import javafx.scene.input.KeyCode
import java.io.Serializable

open class Shortcut(val modifiers: List<KeyCode>, val key: KeyCode) : Serializable {
    val shortcutString: String get() = createShortcutString(modifiers, key)

    companion object {
        fun createShortcutString(modifiers: List<KeyCode>, key: KeyCode?): String {
            val keysPressed = mutableListOf<String>()
            if (modifiers.contains(KeyCode.COMMAND)) {
                keysPressed.add("Cmd")
            }
            if (modifiers.contains(KeyCode.WINDOWS)) {
                keysPressed.add("Win")
            }
            if (modifiers.contains(KeyCode.CONTROL)) {
                keysPressed.add("Ctrl")
            }
            if (modifiers.contains(KeyCode.ALT)) {
                keysPressed.add("Alt")
            }
            if (modifiers.contains(KeyCode.SHIFT)) {
                keysPressed.add("Shift")
            }
            val keyName = key?.name ?: ""
            keysPressed.add(keyName.split("_").joinToString(" ") { it.toLowerCase().capitalize() })

            return keysPressed.joinToString(" + ")
        }
    }
}

class FollowShortcut(modifiers: List<KeyCode>, key: KeyCode) : Shortcut(modifiers, key)

class ChannelPointsShortcut(modifiers: List<KeyCode>, key: KeyCode, val title: String) : Shortcut(modifiers, key)

class CheerShortcut(modifiers: List<KeyCode>, key: KeyCode, val bits: Int) : Shortcut(modifiers, key)

class SubscriptionShortcut(modifiers: List<KeyCode>, key: KeyCode, val months: Int) : Shortcut(modifiers, key)

class GiftSubscriptionShortcut(modifiers: List<KeyCode>, key: KeyCode, val count: Int) : Shortcut(modifiers, key)