object Anagramme {

	def anagrammeUtil(chaine_ : List[Char]): List[List[Char]] = {

		if(chaine_.size == 0) {
			List(chaine_)
		} 
		else {

			for {
				lettre <- chaine_
				lettreSuivante <- anagramme(chaine_ diff List(lettre))
			} yield lettre :: lettreSuivante
		}
	}

	def anagramme(mot_ : List[Char]): List[List[Char]] = {
		val tmp = anagrammeUtil(mot_)
		var ret = tmp.distinct
		return ret
	}
	
	def main(args: Array[String]): Unit = {

		if(args.size != 1) {
			println("Usage : Anagramme <unMot>")
		}
		else {

			val ret : List[List[Char]] = anagramme(args(0).toList)
			
			println(ret)
			println("Nombre d'anagrammes : " + ret.size)
		}
	}
}
