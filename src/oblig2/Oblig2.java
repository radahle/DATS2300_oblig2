/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oblig2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author RudiAndre
 */
public class Oblig2 {

    public static Integer[] randPerm(Integer n) // en effektiv versjon
    {
    
        Random r = new Random();         // en randomgenerator
        Integer[] a = new Integer[n];            // en tabell med plass til n tall

        Arrays.setAll(a, i -> i + 1);    // legger inn tallene 1, 2, . , n

        for (int k = n - 1; k > 0; k--) // løkke som går n - 1 ganger
        {
            int i = r.nextInt(k + 1);        // en tilfeldig tall fra 0 til k
            bytt(a, k, i);                   // bytter om
        }

        return a;                        // permutasjonen returneres
    
    }
    
    public static void bytt(Integer[] a, Integer i, Integer j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    

    public static void main(String[] args) {
        String[] navn = {"Lars", "Anders", "Bodil", "Kari", "Per", "Berit"};
        Integer[] a = randPerm(3000);

        Liste<String> liste1 = new DobbeltLenketListe<>(navn);
        Liste<Integer> liste2 = new DobbeltLenketListe<>(a);
        
        long tid = System.currentTimeMillis();
        DobbeltLenketListe.sorter(liste2, Comparator.naturalOrder());
        long slutt = System.currentTimeMillis() - tid;
        
        System.out.println(slutt);
    }
}
