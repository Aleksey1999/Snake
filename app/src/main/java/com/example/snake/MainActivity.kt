package com.example.snake

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.snake.SnakeCore.gameSpeed
import com.example.snake.SnakeCore.isPlay
import com.example.snake.SnakeCore.startTheGame
import kotlinx.android.synthetic.main.activity_main.*

const val HEAD_SIZE = 100
const val CELLS_ON_FIELD = 7

class MainActivity : AppCompatActivity() {

    private val allTale = mutableListOf<PartOfTale>()

    private val human by lazy {
        ImageView(this)
            .apply {
                this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
                this.setImageResource(R.drawable.ic_person_black_24dp)
            }
    }

    private val head by lazy {
        ImageView (this)
            .apply {
                this.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
                this.setImageResource(R.drawable.snake)
            }
    }

    private var currentDirections = Direction.BOTTOM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        head.background = ContextCompat.getDrawable(this, R.drawable.circle)
        container.layoutParams = LinearLayout.LayoutParams(HEAD_SIZE * CELLS_ON_FIELD, HEAD_SIZE * CELLS_ON_FIELD)

        startTheGame()
        generateNewHuman()
        SnakeCore.nextMove = {
            move(Direction.BOTTOM)
        }

        ivArrowUp.setOnClickListener {
            SnakeCore.nextMove = {
                checkIfCurrentDirectionIsNotOpposite(Direction.UP, Direction.BOTTOM)
            }
        }
        ivArrowBottom.setOnClickListener {
            SnakeCore.nextMove = {
                checkIfCurrentDirectionIsNotOpposite(Direction.BOTTOM, Direction.UP)
            }
        }
        ivArrowLeft.setOnClickListener {
            SnakeCore.nextMove = {
                checkIfCurrentDirectionIsNotOpposite(Direction.LEFT, Direction.RIGHT)
            }
        }
        ivArrowRight.setOnClickListener {
            SnakeCore.nextMove = {
                checkIfCurrentDirectionIsNotOpposite(Direction.RIGHT, Direction.LEFT)
            }
        }
        ivPause.setOnClickListener {
            if (isPlay) {
                ivPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            } else {
                ivPause.setImageResource(R.drawable.ic_pause_black_24dp)
            }
            SnakeCore.isPlay = !isPlay
        }
    }

    private fun checkIfCurrentDirectionIsNotOpposite(properDirections: Direction, oppositeDirections: Direction){
        if (currentDirections == oppositeDirections){
            move(currentDirections)
        }else{
            move(properDirections)
        }
    }

    private fun generateNewHuman() {
        val viewCoordinate = generateHumanCoordinates()
        (human.layoutParams as FrameLayout.LayoutParams).topMargin = viewCoordinate.top
        (human.layoutParams as FrameLayout.LayoutParams).leftMargin = viewCoordinate.left
        container.removeView(human)
        container.addView(human)
    }

    private fun generateHumanCoordinates():ViewCoordinate {
        val viewCoordinate = ViewCoordinate(
            (0 until CELLS_ON_FIELD).random() * HEAD_SIZE,
            (0 until CELLS_ON_FIELD).random() * HEAD_SIZE
        )
        for (partTale in allTale) {
            if (partTale.viewCoordinate == viewCoordinate) {
                return generateHumanCoordinates()
            }
        }
        if (head.top == viewCoordinate.top && head.left == viewCoordinate.left){
            return generateHumanCoordinates()
        }
        return viewCoordinate
    }

    private fun checkIfSnakeEatsPerson() {
        if (head.left == human.left && head.top == human.top) {
            generateNewHuman()
            addPartOfTale(head.top, head.left)
            increaseDifficult()
        }
    }

    private fun increaseDifficult() {
        if (gameSpeed <= MINIMUM_GAME_SPEED) {
            return
        }
        if (allTale.size % 5 == 0){
            gameSpeed -= 100
        }
    }

    private fun addPartOfTale(top:Int, left:Int){
        val talePart = drawPartOfTale(top, left)
        allTale.add(PartOfTale (ViewCoordinate(top, left), talePart))
    }

    private fun drawPartOfTale(top: Int, left: Int): ImageView {
        val taleImage = ImageView(this)
        taleImage.setImageResource(R.drawable.scales)
        taleImage.layoutParams = FrameLayout.LayoutParams(HEAD_SIZE, HEAD_SIZE)
        (taleImage.layoutParams as FrameLayout.LayoutParams).topMargin = top
        (taleImage.layoutParams as FrameLayout.LayoutParams).leftMargin = left

        container.addView(taleImage)
        return taleImage
    }

    fun move(directions: Direction) {
        when (directions) {
            Direction.UP -> moveHeadAndRotate(Direction.UP, 90f, -HEAD_SIZE)
            Direction.BOTTOM -> moveHeadAndRotate(Direction.BOTTOM, 270f, HEAD_SIZE)
            Direction.LEFT -> moveHeadAndRotate(Direction.LEFT, 0f, -HEAD_SIZE)
            Direction.RIGHT -> moveHeadAndRotate(Direction.RIGHT, 180f, HEAD_SIZE)
        }
        runOnUiThread {
            if (checkIfSnakeSmash()){
                isPlay = false
                showScore()
                return@runOnUiThread
            }
            makeTaleMove()
            checkIfSnakeEatsPerson()
            container.removeView(head)
            container.addView(head)
        }
    }

    private fun moveHeadAndRotate(direction: Direction, angle: Float, coordinates: Int) {
        head.rotation = angle
        when (direction) {
            Direction.UP, Direction.BOTTOM -> {
                (head.layoutParams as FrameLayout.LayoutParams).topMargin += coordinates
            }
            Direction.LEFT, Direction.RIGHT -> {
                (head.layoutParams as FrameLayout.LayoutParams).leftMargin += coordinates
            }
        }
        currentDirections = direction
    }

    private fun showScore() {
        AlertDialog.Builder(this)
            .setTitle("Твой счет: ${allTale.size} ")
            .setPositiveButton("ok") { _, _ ->
                this.recreate()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun checkIfSnakeSmash(): Boolean {
        for (talePart in allTale){
            if (talePart.viewCoordinate.left == head.left && talePart.viewCoordinate.top == head.top){
                return true
            }
        }
        if (head.top < 0
            || head.left < 0
            || head.top >= HEAD_SIZE * CELLS_ON_FIELD
            || head.left >= HEAD_SIZE * CELLS_ON_FIELD){
            return true
        }
        return false
    }

    private fun makeTaleMove() {
        var tempTalePart : PartOfTale? = null
        for(index in 0 until allTale.size){
            val talePart = allTale[index]
            container.removeView(talePart.imageView)
            if (index == 0) {
                tempTalePart = talePart
                allTale[index] = PartOfTale(ViewCoordinate(head.top, head.left), drawPartOfTale(head.top, head.left))
            } else {
                val anotherTempPartOfTale = allTale[index]
                tempTalePart?.let {
                    allTale[index] = PartOfTale(it.viewCoordinate, drawPartOfTale(it.viewCoordinate.top, it.viewCoordinate.left))
                }
                tempTalePart = anotherTempPartOfTale
            }
        }
    }
}

enum class Direction {
    UP,
    RIGHT,
    BOTTOM,
    LEFT
}

