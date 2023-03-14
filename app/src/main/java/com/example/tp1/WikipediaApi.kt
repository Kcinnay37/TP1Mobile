import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.util.concurrent.Executors

class WikipediaAPI {
    companion object {
        //l'url pour faire le requete a l'API
        private const val API_URL = "https://fr.wikipedia.org/w/api.php?action=query&format=json&prop=extracts%7Cpageimages&exintro&explaintext&redirects=1&pithumbsize=500&titles="
    }

    //va permettre d'executer l'opperation sur un autre thread
    private val executor = Executors.newSingleThreadExecutor()

    //l'interface pour pouvoir definir quoi faire avec la requete une fois celle si terminer dans les parametre de getPageInfo
    interface OnPageInfoReceivedListener {
        fun onPageInfoReceived(page: WikipediaPage?)
    }

    //va executer la requete et appeler la fonction onPageInfo
    fun GetPageInfo(pageTitle: String, listener: OnPageInfoReceivedListener) {

        //convertit le nom pour pouvoir le mettre dans l'URL
        val titleEncoded = URLEncoder.encode(pageTitle, "UTF-8")
        //Cree le url avec le URL de base + le nom encodé
        val url = URL(API_URL + titleEncoded)

        //sur un autre thread va executer la requete
        executor.execute {
            //ouvre la connection HTTP vers l'API wikipedia
            val connection = url.openConnection() as HttpURLConnection
            //Dit le type de requete effectué
            connection.requestMethod = "GET"

            //Recupere le code de la reponse HTTP de la requete
            val responseCode = connection.responseCode
            //Si la requete est reussi
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                //met la reponse du serveur dans un stringbuilder en lisant la reponse ligne par ligne
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                //va passer la reponse du serveur ligne par ligne et la mettre dans le stringbuilder
                //le also va permettre dexecuter une action sur la valeur retourner et retourner cette valeur
                while (bufferedReader.readLine().also { line = it } != null)
                {
                    //la fonction lambda trim va passer la chaine de char a chaque index et si la condition est vrai il va enlever le char
                    response.append(line!!.trim { it <= ' ' })
                }
                bufferedReader.close()

                //convertie la reponse de l'API en JSON
                val jsonObject = JSONObject(response.toString())
                //prend l'objet query du JSON
                val query = jsonObject.getJSONObject("query")
                //obtient l'object pages du JSON qui contient les info sur les page de la recherche
                val pages = query.getJSONObject("pages")
                //va chercher l'id de la premiere page
                var pageId = pages.keys().next()
                //Obtient l'object de la premiere page
                val page = pages.getJSONObject(pageId)

                //recupere le titre de la page
                val title = page.getString("title")
                //recupere l'extrait de la page si il n'a un
                val extract = if (page.has("extract")) page.getString("extract") else ""

                //recupere url de l'image de la page si il n'a une
                val thumbnailUrl: String
                if (page.has("thumbnail"))
                {
                    thumbnailUrl = page.getJSONObject("thumbnail").getString("source")
                } else
                {
                    thumbnailUrl = ""
                }

                //telecharge l'image de la page et la stock en Bitmap
                val bitmap: Bitmap?
                if (thumbnailUrl != "")
                {
                    val imageUrl = URL(thumbnailUrl)
                    bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
                }
                else
                {
                    bitmap = null;
                }

                //cree la variable qui va avoir tout les information de la page
                val pageInfo = WikipediaPage(title, extract, bitmap)

                //appel la fonction qui va traiter la reponse avec les info de la page en parametre
                listener.onPageInfoReceived(pageInfo)
            }
            else
            {
                //appel la fonction qui va traiter la reponse avec null en parametre
                listener.onPageInfoReceived(null)
            }
        }
    }
}

data class WikipediaPage(val title: String, val extract: String, val image: Bitmap?)