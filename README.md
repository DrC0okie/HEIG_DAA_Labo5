# HEIG_DAA_Labo5



## Question 1

Chaque élément de la RecyclerView est géré par une classe ViewHolder, qui est responsable du lancement et de la gestion de la coroutine pour le téléchargement et l'affichage de l'image associée à cet élément. Voici comment la gestion des coroutines est implémentée :

Dans la classe ViewHolder, une coroutine est démarrée dans la méthode `bind(url: URL)`. Cette coroutine gère le téléchargement et l'affichage de l'image. La coroutine est lancée dans un `LifecycleCoroutineScope` pour garantir qu'elle est liée au cycle de vie de l'activité qui l'héberge.

Pour garantir que chaque élément a une coroutine distincte, une variable `currentUrl` est utilisée. Lorsqu'une nouvelle URL est liée à un `ViewHolder`, il vérifie si cette URL est différente de l'actuelle. Si elle est différente, cela indique que l'élément est sur le point d'afficher une nouvelle image, nécessitant l'annulation de toute coroutine en cours associée à l'image précédente:

```kotlin
        fun bind(url: URL) {
            // Check if the new URL is different from the current one
            if (currentUrl != url.toString()) {
                // Cancel any existing download job for the previous URL
                downloadJob?.cancel()

                // Reset the visibility of the ProgressBar and ImageView
                progressBar.visibility = View.VISIBLE
                image.visibility = View.INVISIBLE

                // Update the current URL
                currentUrl = url.toString()

                // Start a new coroutine for downloading and displaying the image
                downloadJob = scope.launch {
                    val cachedBitmap =
                        getBitmap(url.path.substring(url.path.lastIndexOf('/') + 1), url)
                    updateImageView(cachedBitmap)
                }
            }
        }
```



Le système RecyclerView recycle les vues lorsqu'elles défilent hors de la zone visible. Pour gérer cela, la méthode `onViewRecycled(holder: ViewHolder)` de l'adaptateur est surchargée. Dans cette méthode, la méthode `unbind()` du `ViewHolder` est appelée. Cette méthode est chargée d'annuler toute coroutine active associée au ViewHolder. L'annulation est obtenue en appelant `downloadJob?.cancel()`, qui annule en toute sécurité la coroutine si elle est actuellement active. Cela garantit que tout téléchargement ou traitement d’image en cours est arrêté, libérant ainsi des ressources.

Finalement, dans la méthode `unbind()`, la visibilité de ProgressBar et d'ImageView est réinitialisée. Cela prépare le ViewHolder à la réutilisation avec un nouvel élément, garantissant ainsi un état cohérent. 

```Kotlin
    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }
```

```kotlin
        fun unbind() {
            // Cancel any ongoing download job to prevent memory leaks and unnecessary work
            downloadJob?.cancel()

            // Reset the visibility of the ProgressBar and ImageView
            progressBar.visibility = View.VISIBLE
            image.visibility = View.INVISIBLE

            // Clear the current URL since the view is being recycled
            currentUrl = null
        }
```



Grâce à cette implémentation, nous garantissons que toute coroutine associée à un élément RecyclerView est gérée correctement. Lorsqu'un élément est recyclé, sa coroutine associée est annulée, empêchant toute opération en arrière-plan qui n'est plus nécessaire.



## Question 2

Pour aligner notre gestion des coroutines sur le cycle de vie de l'activité, nous avons utilisé le `lifecycleScope`,  un `CoroutineScope` prédéfini lié au cycle de vie de l'activité. Cette portée garantit que toutes les coroutines lancées au sein de celle-ci sont automatiquement annulées lorsque l'activité atteint la phase `onDestroy()` de son cycle de vie.

Bien que `lifecycleScope` annule automatiquement les coroutines sur `onDestroy()`, nous avons encore renforcé ce comportement pour gérer explicitement l'annulation des coroutines. Nous l'avons fait en surchargeant la méthode `onDestroy()` dans notre MainActivity:

````Kotlin
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // This ensures that coroutines are cancelled only when the activity is truly finishing
            lifecycleScope.coroutineContext.cancelChildren()
        }
    }
````



Dans cette implémentation, nous invoquons `CancelChildren()` sur le `CoroutineContext` de `lifecycleScope`. Cette méthode annule toutes les coroutines démarrées dans le lifecycleScope. La vérification `if (isFinishing)` garantit que les coroutines ne sont annulées que lorsque l'activité est réellement terminée et pas seulement en cours de changement de configuration (comme la rotation de l'écran). Cette distinction est importante pour éviter l'annulation prématurée des coroutines, qui pourrait encore être nécessaire si l'activité est simplement recréée.

**Est-ce que cette solution est la plus adaptée?** N'étant pas experts Android, il est difficile de répondre à cette question, cependant nous pouvons apporter quelques éléments de réponse:

* Si nos coroutines doivent se terminer quel que soit le cycle de vie de l'activité (par exemple, la synchronisation des données en arrière-plan qui doit se poursuivre pendant les modifications de configuration), une portée différente comme `ViewModelScope` ou un `CoroutineScope` personnalisé lié au cycle de vie de l'application peut être plus approprié.
*  L'utilisation de `isFinishing` dans `onDestroy()` est cruciale pour faire la différence entre la destruction d'activité due aux changements de configuration et la destruction finale. Si notre application gère les modifications de configuration (comme les rotations d'écran) sans recréer l'activité, notre approche fonctionne bien.
* Pour les tâches ou opérations de longue durée qui doivent survivre à l'activité, nous pouvons envisager d'utiliser un `WorkManager` ou des services.



## Question 3

