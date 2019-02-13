package net.capellari.julien.data.utils

import net.capellari.julien.data.Converter
import net.capellari.julien.data.Noeud
import net.capellari.julien.data.Property
import net.capellari.julien.data.linkTo

class StringToIntConverter(noeud: Noeud<String>)
    : Converter<String,Int>(StringToIntTransform(), ToStringTransform(), noeud) {

    // Propriétés
    @Property var max by linkTo<Int>(f2t)
    @Property var min by linkTo<Int>(f2t)
}