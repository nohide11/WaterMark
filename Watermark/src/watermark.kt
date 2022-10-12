package watermark
import java.awt.Color
import java.awt.Transparency.TRANSLUCENT
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import kotlin.system.exitProcess

const val ZERO = 0
fun main() {
    println("Input the image filename:")
    val name = readLine()
    val fileName = File(name)
    try {
        val image = ImageIO.read(fileName)
    } catch (e: Exception) {
        println("The file $name doesn't exist.")
        exitProcess(0)
    }
    val image = ImageIO.read(fileName)
    if (image.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        exitProcess(0)
    }
    if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
        println("The image isn't 24 or 32-bit.")
        exitProcess(0)
    }


    println("Input the watermark image filename:")
    val watermarkName = readLine()

    try {
        val watermark = ImageIO.read(File(watermarkName))
    } catch (e: Exception) {
        println("The file $watermarkName doesn't exist.")
        exitProcess(0)
    }
    val watermark = ImageIO.read(File(watermarkName))
    var input = " "


    if (watermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        exitProcess(0)
    }
    if (watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) {
        println("The watermark isn't 24 or 32-bit.")
        exitProcess(0)
    }
    if (image.width < watermark.width || image.height < watermark.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }

    var rYes= " "
    val myList: MutableList<String>
    var backgroundColor = Color(0, 0, 0)
    if (watermark.transparency == TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        input = readLine().toString().lowercase()
    }
    else {
        println("Do you want to set a transparency color?")
        rYes = readLine().toString()
        if (rYes == "yes") {
            val reg = Regex(pattern = "[0-2]?[0-5]?[0-5]")
            println("Input a transparency color ([Red] [Green] [Blue]):")
            myList = readLine() !!.split(" ").toMutableList()
            for (i in myList) {
                if (!reg.matches(i) || myList.size != 3) {
                    println("The transparency color input is invalid.")
                    exitProcess(0)
                }
            }
            backgroundColor = Color(myList[0].toInt(), myList[1].toInt(), myList[2].toInt())
        }
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val transparency = readLine().toString()
    if (transparency.toIntOrNull() == null) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    else if (transparency.toInt() !in 0 ..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }

    println("Choose the position method (single, grid):")
    val positionWatermark = readLine().toString()
    if (positionWatermark != "single" && positionWatermark != "grid") {
        println("The position method input is invalid.")
        exitProcess(0)
    }
    var posY = 0
    var posX = 0
    if (positionWatermark == "single") {
        println("Input the watermark position ([x 0-${image.width - watermark.width}] [y 0-${image.height - watermark.height}]):")
        val XY: MutableList<String> = readLine() !!.split(" ").toMutableList()
        if (XY[0].toIntOrNull() == null) {
            println("The position input is invalid.")
            exitProcess(0)
        }
        if (XY[1].toIntOrNull() == null) {
            println("The position input is invalid.")
            exitProcess(0)
        }
        posX = XY[0].toInt()
        posY = XY[1].toInt()
        val width = image.width - watermark.width
        val height = image.height - watermark.height
        if (posX !in 0 .. width || posY !in 0 .. height){
            println("The position input is out of range.")
            exitProcess(0)
        }
    }

    println("Input the output image filename (jpg or png extension):")
    val outputOp = readLine().toString()
    val regex = Regex(pattern = ".+\\.(jp|pn)g")
    if (!regex.matches(outputOp)) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }
    val fileOut = File(outputOp)

    val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x, y))
            var w = Color(image.getRGB(x, y))


            if (input == "yes") {
                if (positionWatermark == "single") {
                    if ((x in posX until posX + watermark.width) && (y in posY until posY + watermark.height)) {
                        w = Color(watermark.getRGB(x - posX, y - posY), true)
                    }
                } else if (positionWatermark == "grid") {
                    w = Color(watermark.getRGB(x % watermark.width, y % watermark.height), true)
                }
            }
            else {
                if (positionWatermark == "single") {
                    if ((x in posX until posX + watermark.width) && (y in posY until posY + watermark.height)) {
                        w = Color(watermark.getRGB(x - posX, y - posY))
                    }
                }
                else if (positionWatermark == "grid")  {
                    w = Color(watermark.getRGB(x % watermark.width, y % watermark.height))
                }
            }

            val color = if (rYes == "yes" && w == backgroundColor) {
                Color(i.red, i.green, i.blue)
            } else {
                Color(
                    (transparency.toInt() * w.red + (100 - transparency.toInt()) * i.red) / 100,
                    (transparency.toInt() * w.green + (100 - transparency.toInt()) * i.green) / 100,
                    (transparency.toInt() * w.blue + (100 - transparency.toInt()) * i.blue) / 100
                )
            }
            if (w.alpha == 0) output.setRGB(x, y, Color(image.getRGB(x, y)).rgb)
            else output.setRGB(x, y, color.rgb)
        }
    }
    ImageIO.write(output, "png", fileOut)
    println("The watermarked image $outputOp has been created.")

}