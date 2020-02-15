package test

import java.awt.Desktop
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*
class Oo {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater { UserUI("User") }
        }
    }
}

fun main2(user: String) {
    println("Choose: $user")
    val msg = Main.Initiliazer(user)
    val imageURL = File("images/images.txt").readLines()
    val basePath = "images/"
    val paths = ArrayList<String>()

    println("Started getting images")
    for ((i, url) in imageURL.withIndex()) {
        val p = "${basePath}book$i"
        paths.add(p)
            saveImage(getImageFromURL(url), p)
    }

    println("Image getting completed")
    val bookURL = File("books.txt").readLines()


    EventQueue.invokeLater { JLabelExample("GOODREADS RECOMMENDATION SYSTEM", msg, paths.zip(bookURL)) }
}

/*fun getImageFromURL(path: String): BufferedImage? {
    val url = URL(path)
    println(url)
    var result: BufferedImage? = null
    try {
        result = ImageIO.read(url)
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        println(e.message)
        println(e.cause)
    }
    return result
}*/
fun getImageFromURL(path: String): BufferedImage = ImageIO.read(URL(path)) ?: throw Exception()
fun saveImage(img: BufferedImage, path: String): Boolean = ImageIO.write(img, "jpg", File(path))
fun openWebPage2(uri: URI) = (Desktop.getDesktop() ?: throw Exception()).browse(uri)
fun openWebPage(uri: URI) = Runtime.getRuntime().exec(arrayOf("google-chrome", uri.toString())) //for linux

class UserUI(title: String) : JFrame() {
    init {
        initUI(title)
    }

    private fun initUI(title: String) {
        setTitle(title)
        setLocationRelativeTo(null)
        setSize(960, 640)
        isVisible = true
        isResizable = false
        this.contentPane = JPanel()
        createLayout()
    }

    private fun createLayout() {
        mapOf("User1" to "1", "User2" to "2", "User3" to "3").map { e ->
            contentPane.add(JButton(e.key).apply {
                addActionListener {
                    isVisible = false
                    dispose()
                    main2(e.value)
                }
            })
        }
    }
}

class JLabelExample(title: String, msg: String, imagePaths: List<Pair<String, String>>, page: Int = 1) : JFrame() {
    init {
        initUI(title, msg, imagePaths, page)
    }

    private fun initUI(
            title: String,
            msg: String,
            imagePaths: List<Pair<String, String>>,
            page: Int,
            nButton: Boolean = true,
            pButton: Boolean = false
    ) {
        setTitle(title)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                Main.deleteFile()
            }
        })
        setLocationRelativeTo(null)
        setSize(960, 640)
        isVisible = true
        isResizable = false

        val labels = imagePaths.map { book ->
            JButton(ImageIcon(book.first)).apply {
                minimumSize = Dimension(96, 96)
                maximumSize = Dimension(128, 192)
                addActionListener {
                    openWebPage2(URI(book.second))
                }
            }
        }

        this.contentPane = JPanel()

        createLayout(title, msg, imagePaths, labels, page, nButton, pButton)
    }

    private fun createLayout(
            title: String,
            msg: String,
            imagePaths: List<Pair<String, String>>,
            labels: List<JButton>,
            i: Int,
            nButton: Boolean = true,
            pButton: Boolean = false
    ) {
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        val page = i - 1
        layout.autoCreateContainerGaps = true
        layout.autoCreateGaps = true

        val nextButton = JButton("Next").apply {
            minimumSize = Dimension(96, 96)
            maximumSize = Dimension(128, 192)
            isEnabled = nButton
        }

        val prevButton = JButton("Previous").apply {
            minimumSize = Dimension(96, 96)
            maximumSize = Dimension(128, 192)
            isEnabled = pButton
        }

        val infoButton = JButton("Info").apply {
            minimumSize = Dimension(96, 96)
            maximumSize = Dimension(128, 192)
        }

        infoButton.addActionListener {
            JOptionPane.showMessageDialog(
                    null, msg
            )
        }

        if (page + 1 < imagePaths.size / 12) {
            nextButton.addActionListener {
                if (page == imagePaths.size / 12 - 2) {
                    initUI(title, msg, imagePaths, i + 1, nButton = false, pButton = true)
                } else {
                    initUI(title, msg, imagePaths, i + 1, nButton = true, pButton = true)
                }
            }
        }

        if (page > 0) {
            prevButton.addActionListener {
                if (page == 1) {
                    initUI(title, msg, imagePaths, i - 1, nButton = true, pButton = false)
                } else {
                    initUI(title, msg, imagePaths, i - 1, nButton = true, pButton = true)
                }
            }
        }

        layout.setHorizontalGroup(layout.createParallelGroup().apply {
            addGroup(layout.createSequentialGroup().apply {
                labels.slice(0 + 12 * page..5 + 12 * page).forEach { addComponent(it) }
            })
            addGroup(layout.createSequentialGroup().apply {
                labels.slice(6 + 12 * page..11 + 12 * page).forEach { addComponent(it) }
            })
            addGroup(layout.createSequentialGroup().apply {
                addComponent(prevButton); addComponent(nextButton); addComponent(
                    infoButton
            )
            })
        })

        layout.setVerticalGroup(layout.createSequentialGroup().apply {
            addGroup(layout.createParallelGroup().apply {
                labels.slice(0 + 12 * page..5 + 12 * page).forEach { addComponent(it) }
            })
            addGroup(layout.createParallelGroup().apply {
                labels.slice(6 + 12 * page..11 + 12 * page).forEach { addComponent(it) }
            })
            addGroup(layout.createParallelGroup().apply {
                addComponent(prevButton); addComponent(nextButton); addComponent(
                    infoButton
            )
            })
        })

        pack()
    }
}