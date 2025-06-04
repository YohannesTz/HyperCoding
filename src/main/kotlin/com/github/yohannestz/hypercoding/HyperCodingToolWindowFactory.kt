package com.github.yohannestz.hypercoding

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.*
import java.awt.event.*
import java.net.URI
import javax.swing.*
import javax.swing.Timer

class HyperCodingToolWindowFactory : ToolWindowFactory {

    private var timer: Timer? = null

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = JBCefBrowser("https://www.youtube.com/shorts/FT2Rn-JZuTM")

        val platformCombo = ComboBox(arrayOf("Family Guy", "Subway Surfers", "Temple Run")).apply {
            maximumSize = Dimension(Int.MAX_VALUE, 30)
            addActionListener {
                val url = when (selectedItem as String) {
                    "Subway Surfers" -> "https://www.youtube.com/shorts/FT2Rn-JZuTM"
                    "Temple Run" -> "https://www.youtube.com/shorts/rrYOX_WRG_M"
                    else -> "https://www.youtube.com/shorts/I7crOjG6ssk"
                }
                browser.loadURL(url)
            }
        }

        val autoScrollCheckBox = JBCheckBox("Auto Scroll ↓")
        val delayField = JBTextField("5").apply {
            maximumSize = Dimension(60, 30)
            isEnabled = false
        }

        autoScrollCheckBox.addActionListener {
            delayField.isEnabled = autoScrollCheckBox.isSelected
            if (autoScrollCheckBox.isSelected) {
                val delaySeconds = delayField.text.toIntOrNull() ?: 5
                timer = Timer(delaySeconds * 1000) {
                    println("Auto-scroll: pressing ↓")
                    val component = browser.cefBrowser.uiComponent
                    val event = KeyEvent(
                        component,
                        KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(),
                        0,
                        KeyEvent.VK_DOWN,
                        KeyEvent.CHAR_UNDEFINED
                    )
                    component.dispatchEvent(event)
                }.also { it.start() }
            } else {
                timer?.stop()
                timer = null
            }
        }

        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript(
                    """
                                            setTimeout(() => {
                                                const btn = document.querySelector('yt-button-shape#fullscreen-button-shape button');
                                                if (btn) {
                                                    console.log("clicking fullscreen button");
                                                    btn.click();
                                                } else {
                                                    console.log("fullscreen button not found");
                                                }
                                            }, 1000);
                                            """.trimIndent(),
                    browser.url,
                    0
                )
            }
        }, browser.cefBrowser)

        val controlPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(platformCombo)
            add(Box.createHorizontalStrut(10))
            add(autoScrollCheckBox)
            add(Box.createHorizontalStrut(5))
            add(JLabel("Delay (sec):"))
            add(delayField)
        }

        val browserContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            background = JBColor.PanelBackground
            add(Box.createHorizontalStrut(30))
            add(browser.component.apply {
                preferredSize = Dimension(360, 640)
                minimumSize = preferredSize
            })
            add(Box.createHorizontalStrut(30))
        }

        val githubButton = JButton("GitHub").apply {
            maximumSize = Dimension(100, 30)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                Desktop.getDesktop().browse(URI("https://github.com/yohannesTz"))
            }
        }

        val container = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = JBColor.PanelBackground
            add(controlPanel)
            add(Box.createVerticalStrut(10))
            add(browserContainer)
            add(Box.createVerticalStrut(10))
            add(githubButton)
        }

        val content = ContentFactory.getInstance().createContent(container, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
