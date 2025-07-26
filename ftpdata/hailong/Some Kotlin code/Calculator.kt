package com.example.calculator

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class MainActivity : AppCompatActivity() {
    private lateinit var expression: TextView
    private lateinit var result: TextView
    private lateinit var calculation: String

    private lateinit var clear: Button
    private lateinit var delete: Button

    private lateinit var add: Button
    private lateinit var subtract: Button
    private lateinit var multiply: Button
    private lateinit var divide: Button

    private lateinit var equal: Button
    private lateinit var percent: Button
    private lateinit var plusminus: Button
    private lateinit var comma: Button

    private lateinit var zero: Button
    private lateinit var one: Button
    private lateinit var two: Button
    private lateinit var three: Button
    private lateinit var four: Button
    private lateinit var five: Button
    private lateinit var six: Button
    private lateinit var seven: Button
    private lateinit var eight: Button
    private lateinit var nine: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expression = findViewById(R.id.expression)
        result = findViewById(R.id.Result)

        clear = findViewById(R.id.clear)
        delete = findViewById(R.id.delete)

        add = findViewById(R.id.add)
        subtract = findViewById(R.id.subtract)
        multiply = findViewById(R.id.multiply)
        divide = findViewById(R.id.divide)

        equal = findViewById(R.id.equal)
        percent = findViewById(R.id.percent)
        plusminus = findViewById(R.id.plusminus)
        comma = findViewById(R.id.comma)

        zero = findViewById(R.id.zero)
        one = findViewById(R.id.one)
        two = findViewById(R.id.two)
        three = findViewById(R.id.three)
        four = findViewById(R.id.four)
        five = findViewById(R.id.five)
        six = findViewById(R.id.six)
        seven = findViewById(R.id.seven)
        eight = findViewById(R.id.eight)
        nine = findViewById(R.id.nine)

        expression.movementMethod = ScrollingMovementMethod()
        expression.isActivated = true
        expression.isPressed = true

        clear.setOnClickListener {
            setExpression ("0")
            result.text = "0"
        }

        delete.setOnClickListener {
            val oldStr = expression.text.toString()
            if (oldStr.isNotEmpty()) {
                val endIndex = oldStr.lastIndex
                setExpression(oldStr.substring(0, endIndex))
                calculation = calculation.substring(0, endIndex)
            }
        }

        add.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', '*', '/', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "+"
                setExpression(oldStr + "+")
            }
        }

        subtract.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', 'x', '÷', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "-"
                setExpression(oldStr + "-")
            }
        }

        multiply.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', 'x', '÷', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "*"
                setExpression(oldStr + "x")
            }

            setExpression(oldStr + "x")
        }

        divide.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', 'x', '÷', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "/"
                setExpression(oldStr + "÷")
            }

        }

        equal.setOnClickListener {
            getResult()
        }

        percent.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', 'x', '÷', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "%"
                setExpression(oldStr + "%")
            }
        }

        plusminus.setOnClickListener {  }

        comma.setOnClickListener {
            val oldStr = expression.text.toString()
            val endChars = setOf('+', '-', 'x', '÷', '.', '%')

            if (oldStr.isEmpty() || endChars.any {oldStr.endsWith(it.toString())}) {
                setExpression(oldStr)
            }
            else {
                calculation += "."
                setExpression(oldStr + ".")
            }
        }

        zero.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                calculation = "0"
                setExpression("0")
            }
            else {
                calculation += "0"
                setExpression(oldStr + "0")
            }
        }

        one.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "1"))
                calculation = "1"
            }
            else {
                calculation += "1"
                setExpression(oldStr + "1")
            }
        }

        two.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "2"))
                calculation = "2"
            }
            else {
                calculation += "2"
                setExpression(oldStr + "2")
            }
        }

        three.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "3"))
                calculation = "3"
            }
            else {
                calculation += "3"
                setExpression(oldStr + "3")
            }
        }

        four.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "4"))
                calculation = "4"
            }
            else {
                calculation += "4"
                setExpression(oldStr + "4")
            }
        }

        five.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "5"))
                calculation = "5"
            }
            else {
                calculation += "5"
                setExpression(oldStr + "5")
            }
        }

        six.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "6"))
                calculation = "6"
            }
            else {
                calculation += "6"
                setExpression(oldStr + "6")
            }
        }

        seven.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "7"))
                calculation = "7"
            }
            else {
                calculation += "7"
                setExpression(oldStr + "7")
            }
        }

        eight.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "8"))
                calculation = "8"
            }
            else {
                calculation += "8"
                setExpression(oldStr + "8")
            }
        }

        nine.setOnClickListener {
            val oldStr = expression.text.toString()

            if (oldStr.startsWith("0")) {
                setExpression(oldStr.replace("0", "9"))
                calculation = "9"
            }
            else {
                calculation += "9"
                setExpression(oldStr + "9")
            }
        }
    }

    private fun executeJavaScript(script: String): Any? {
        val context = Context.enter()
        context.optimizationLevel = -1

        return try {
            val scope: Scriptable = context.initStandardObjects()
            context.evaluateString(scope, script, "JavaScript", 1, null)
        } finally {
            Context.exit()
        }
    }

    private fun setExpression(str: String) {
        expression.text = str
    }

    private fun getResult() {
        try {
            val answer = executeJavaScript(calculation).toString()

            if (calculation.length == 0)
                result.text = "0"

            if (answer.endsWith(".0"))
                result.text = answer.replace(".0", "")
            else
                result.text = answer
        }

        catch (e: Exception) {
            result.text = "ERROR"
        }
    }
}