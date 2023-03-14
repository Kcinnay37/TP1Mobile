package com.example.tp1

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.*
import kotlin.random.Random

//l'interface pour appeler les fonction lorsque le joueur pert et gagne
interface OnGameEventListener
{
    fun OnWinGame()
    fun OnLosePoint()
}

class GameView(context: Context, val listener: OnGameEventListener, nbRow: Int, nbCol: Int, sizeX: Int, sizeY: Int, paddingSize: Int, maxValue: Int) : LinearLayout(context), View.OnClickListener
{
    private lateinit var listButton: MutableList<Button>
    private lateinit var dictText: HashMap<Int, String>
    private lateinit var dictValue: HashMap<Int, Int>

    private lateinit var dictButtonTurned: HashMap<Int, Int>
    private lateinit var listCurrButtonTurn: MutableList<Int>

    init
    {
        orientation = VERTICAL
        //va permettre d'alligner au centre verticalement
        gravity = Gravity.CENTER

        listButton = mutableListOf<Button>()

        dictButtonTurned = HashMap<Int, Int>()
        listCurrButtonTurn = mutableListOf<Int>()

        //va cree la grid de bouton a l'ecran
        for (i in 0 until nbRow)
        {
            var rowLayout = LinearLayout(context)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            //va permettre d'alligner au centre horizontalement
            rowLayout.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until nbCol)
            {
                val button = Button(context)
                button.id = View.generateViewId()
                button.setOnClickListener(this)
                button.text = "?"
                button.layoutParams = ViewGroup.LayoutParams(sizeX, sizeY)
                button.setPadding(paddingSize, paddingSize, paddingSize, paddingSize)
                listButton.add(button)
                rowLayout.addView(button)
            }
            addView(rowLayout)
        }

        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        dictText = HashMap<Int, String>()
        dictValue = HashMap<Int, Int>()

        //va cree la list de tout les index valide pour ajouter un valeur
        var indexAvailable = mutableListOf<Int>()
        for(i in 0 until listButton.size)
        {
            indexAvailable.add(i)
        }

        //va aller ajouter une valeur dans deux case sois en calcule ou en valeur brut
        for(i in 0 until listButton.size / 2)
        {
            var value: Int = Random.nextInt(0, maxValue)

            for(j in 0 until 2)
            {
                var index : Int = 0;

                if(indexAvailable.size - 1 != 0)
                {
                    index = Random.nextInt(0, indexAvailable.size - 1)
                }

                dictValue.put(listButton[indexAvailable[index]].id, value);

                val textChoice = listOf(0, 1).random()
                if (textChoice == 0)
                {
                    dictText.put(listButton[indexAvailable[index]].id, value.toString())
                }
                else
                {
                    var firstValue = Random.nextInt(0, maxValue + maxValue)

                    var secondValue = value - firstValue

                    if(secondValue < 0)
                    {
                        dictText.put(listButton[indexAvailable[index]].id, firstValue.toString() + " - " + (-secondValue).toString())
                    }
                    else
                    {
                        dictText.put(listButton[indexAvailable[index]].id, firstValue.toString() + " + " + secondValue.toString())
                    }
                }
                indexAvailable.removeAt(index)
            }
        }
    }

    var canClick : Boolean = true
    override fun onClick(p0: View?) {
        if(!dictButtonTurned.containsKey(p0?.id) && canClick)
        {
            canClick = false

            dictButtonTurned.put(p0?.id!!, dictValue[p0?.id]!!)
            (p0 as Button).text = dictText[p0?.id]
            listCurrButtonTurn.add(p0?.id!!)

            //si il a deux bouton de selectionné
            if(listCurrButtonTurn.size == 2)
            {
                //fait une coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)

                    //sur le thread principa;
                    withContext(Dispatchers.Main) {
                        //si les bouton sont egal fiat les actons qui en decoule
                        if(CheckCurrButton())
                        {
                            listCurrButtonTurn.clear()
                            canClick = true

                            if(dictButtonTurned.size == listButton.size)
                            {
                                listener.OnWinGame()
                            }
                        }
                        //sinon fais les action qui en decoule
                        else
                        {

                            val button1 = findViewById<Button>(listCurrButtonTurn[0])
                            val button2 = findViewById<Button>(listCurrButtonTurn[1])
                            button1.text = "?"
                            button2.text = "?"

                            dictButtonTurned.remove(listCurrButtonTurn[0])
                            dictButtonTurned.remove(listCurrButtonTurn[1])

                            listCurrButtonTurn.clear()
                            canClick = true

                            listener.OnLosePoint()
                        }
                    }
                }
            }
            else
            {
                canClick = true
            }
        }
    }

    //compare la valeur des deux bouton actif
    fun CheckCurrButton() : Boolean
    {
        var value1 = dictValue[listCurrButtonTurn[0]]
        var value2 = dictValue[listCurrButtonTurn[1]]

        if(value1 == value2) return true else return false
    }


}