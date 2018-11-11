package sbmk.tommy

import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import tornadofx.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainView : View() {
	override val root: AnchorPane by fxml("/fxml/mainView.fxml")
	private val sendButton: Button by fxid("sendButton")
	private val textField: TextField by fxid("textField")
	private val textArea: TextArea by fxid("textArea")
	private val text: Label by fxid("text")
	private val taskKillButton: Button by fxid("taskKillButton")
	
	private var modeOption = 0
	private var ipAddress = ""
	private val port = 4649
	
	
	init {
		title = "Welcome to our java socket hacking tool with kotlin !!!"
		
		textArea.text = "接続したいパソコンのローカルipアドレスを入力してください。"
		text.text = """
			＜コマンド一覧＞
			・/Command : コマンドプロンプトで実行するコマンドをかくことで実行できる。
			・/CreateFile : ファイルを生成する。このコマンドを受け付けた後は、ファイルの名前指定（拡張子込み）→ ファイルの中身をかく → /Go で保存
			・/Disconnect : 接続していたパソコンから切断する。
		""".trimIndent()
		
		sendButton.action {
			sendAction()
		}
		
		textField.setOnKeyPressed {
			onKeyPressed(it)
		}
		
		taskKillButton.action {
			taskKillAction()
		}
	}
	
	private fun taskKillAction() {
		try {
			when (modeOption) {
				0 -> {
					createErrorAlert("ipアドレスを入力してパソコンに接続してください。")
				}
				
				1 -> {
					val socket = Socket(ipAddress, port)
					val output = PrintWriter(socket.getOutputStream())
					output.println("/CreateTaskKillFile")
					output.flush()
					textArea.text = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
					output.close()
					socket.close()
				}
			}
		} catch (e: Exception) {
			createErrorAlert(e.printStackTrace().toString())
		}
	}
	
	
	private fun onKeyPressed(keyEvent: KeyEvent) {
		if (keyEvent.code == KeyCode.ENTER) sendAction()
	}
	
	
	private fun sendAction() {
		try {
			when (modeOption) {
				0 -> {
					this.ipAddress = textField.text
					textField.text = ""
					val socket = Socket(ipAddress, port)
					val output = PrintWriter(socket.getOutputStream())
					output.println("接続要請")
					output.flush()
					textArea.text = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
					this.modeOption++
					output.close()
					socket.close()
				}
				
				1 -> {
					val request = textField.text
					textField.text = ""
					val socket = Socket(ipAddress, port)
					
					when (request) {
						"/Disconnect" -> {
							textArea.text = "接続したいパソコンのローカルipアドレスを入力してください。"
							val output = PrintWriter(socket.getOutputStream())
							output.println("切断します")
							output.flush()
							modeOption = 0
							this.ipAddress = ""
							output.close()
							socket.close()
						}
						
						else -> {
							val output = PrintWriter(socket.getOutputStream())
							output.println(request)
							output.flush()
							textArea.text = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
							output.close()
							socket.close()
						}
					}
				}
			}
		} catch (e: Exception) {
			createErrorAlert(e.printStackTrace().toString())
		}
	}
	
	
	private fun createErrorAlert(e: String) {
		val alert = Alert(Alert.AlertType.INFORMATION)
		alert.headerText = null
		alert.contentText = "エラーが発生しました。\n $e"
		alert.title = "エラー"
		alert.showAndWait()
	}
}