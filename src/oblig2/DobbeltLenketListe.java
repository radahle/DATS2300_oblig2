package oblig2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;

public class DobbeltLenketListe<T> implements Liste<T> {

    private static final class Node<T> // en indre nodeklasse
    {
        // instansvariabler

        private T verdi;
        private Node<T> forrige, neste;

        private Node(T verdi, Node<T> forrige, Node<T> neste) // konstruktør
        {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        protected Node(T verdi) // konstruktør
        {
            this(verdi, null, null);
        }

    } // Node

    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;   // antall endringer i listen

    // hjelpemetode
    private Node<T> finnNode(int indeks) {
        Node<T> p;

        if (indeks < antall / 2) {
            p = hode;
            for (int i = 0; i < indeks; i++) {
                p = p.neste;
            }
        } else {
            p = hale;
            for (int i = antall - 1; i > indeks; i--) {
                p = p.forrige;
            }
        }
        return p;
    }

    private static void fratilKontroll(int antall, int fra, int til) {
        if (fra < 0) { // fra er negativ
            throw new IndexOutOfBoundsException("fra(" + fra + ") er negativ!");
        }

        if (til > antall) { // til er utenfor tabellen
            throw new IndexOutOfBoundsException("til(" + til + ") > antall(" + antall + ")");
        }

        if (fra > til) { // fra er større enn til
            throw new IllegalArgumentException("fra(" + fra + ") > til(" + til + ") - illegalt intervall!");
        }
    }

    // konstruktør
    public DobbeltLenketListe() {
        hode = hale = null;
        antall = 0;
        endringer = 0;
    }

    // konstruktør
    public DobbeltLenketListe(T[] a) {
        this();

        int lengde = a.length;
        int i = 0;
        for (; i < lengde && a[i] == null; i++);

        Objects.requireNonNull(a, "Tabellen er null!");

        if (i < lengde) {
            Node<T> p = hode = new Node<>(a[i], null, null);
            antall = 1;

            for (i++; i < lengde; i++) {
                if (a[i] != null) {
                    p.neste = new Node<>(a[i], null, null);
                    p.neste.forrige = p;
                    p = p.neste;
                    antall++;
                }
            }
            hale = p;
        }
    }

    // subliste
    public Liste<T> subliste(int fra, int til) {
        fratilKontroll(antall, fra, til);

        DobbeltLenketListe<T> liste = new DobbeltLenketListe<>();
        Node<T> p = finnNode(fra);

        for (int i = fra; i < til; i++) {
            liste.leggInn(p.verdi);
            p = p.neste;
        }
        return liste;
    }

    @Override
    public int antall() {
        return antall;
    }

    @Override
    public boolean tom() {
        return antall == 0;
    }

    @Override
    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Tabellen er null!");

        if (tom()) {
            hale = hode = new Node<>(verdi, null, null);
        } else {
            hale = hale.neste = new Node<>(verdi, hale, null);
        }
        antall++;
        endringer++;
        return true;
    }

    @Override
    public void leggInn(int indeks, T verdi) {
        Objects.requireNonNull(verdi, "Ikke tillatt med null-verdier!");
        indeksKontroll(indeks, true);

        if (tom()) {
            hale = hode = new Node<>(verdi, null, null);
        } else if (indeks == 0) {
            hode = hode.forrige = new Node<>(verdi, null, hode);
        } else if (indeks == antall) {
            hale = hale.neste = new Node<>(verdi, hale, null);
        } else {
            Node<T> p = finnNode(indeks);
            p.forrige = p.forrige.neste = new Node<>(verdi, p.forrige, p);
        }
        antall++;
        endringer++;
    }

    @Override
    public boolean inneholder(T verdi) {
        return indeksTil(verdi) != -1;
    }

    @Override
    public T hent(int indeks) {
        indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }

    @Override
    public int indeksTil(T verdi) {
        if (verdi == null) {
            return -1;
        }

        Node<T> p = hode;

        for (int indeks = 0; indeks < antall; indeks++) {
            if (p.verdi.equals(verdi)) {
                return indeks;
            }
            p = p.neste;
        }
        return -1;
    }

    @Override
    public T oppdater(int indeks, T nyverdi) {
        Objects.requireNonNull(nyverdi, "nullverdi");
        indeksKontroll(indeks, false);

        Node<T> p = finnNode(indeks);
        T gammelverdi = p.verdi;

        p.verdi = nyverdi;

        endringer++;
        return gammelverdi;
    }

    @Override
    public boolean fjern(T verdi) {
        if (verdi == null) {
            return false;          // ingen nullverdier i listen
        }
        Node<T> q = hode, p = null;               // hjelpepekere

        while (q != null) // q skal finne verdien t
        {
            if (q.verdi.equals(verdi)) {
                break;       // verdien funnet
            }
            p = q;
            q = q.neste;                     // p er forgjengeren til q
        }

        if (q == null) {
            return false;              // fant ikke verdi
        } else if (q == hode) {
            hode = hode.neste;    // går forbi q
        } else {
            p.neste = q.neste;                   // går forbi q
        }
        if (q == hale) {
            hale = p;                  // oppdaterer hale
        }
        q.verdi = null;                           // nuller verdien til q
        q.neste = null;                           // nuller nestepeker

        antall--;                                 // en node mindre i listen
        endringer++;

        return true;
    }

    @Override
    public T fjern(int indeks) {
        indeksKontroll(indeks, false);

        T temp;

        if (indeks == 0) {
            temp = hode.verdi;
            if (antall == 1) {
                hale = null;
                hode = hode.neste;
            } else {
                hode = hode.neste;
                hode.forrige.neste = null;
                hode.forrige = null;
            }
        } else {
            Node<T> p = finnNode(indeks - 1);
            Node<T> q = p.neste;
            temp = q.verdi;

            if (q == hale) {
                hale = p;
                p.neste = q.neste;
                q.neste = null;
                q.forrige = null;
            } else {
                p.neste = q.neste;
                p.neste.forrige = p;
                q.neste = null;
                q.forrige = null;
            }
        }
        antall--;
        endringer++;
        return temp;
    }

    @Override
    public void nullstill() {
        Node<T> p = hode;
        Node<T> q;
        for (int i = 0; i < antall - 1; i++) {
            q = p.neste;
            p.neste = null;
            p.forrige = null;
            p = null;
            p = q;
        }
        hode = hale = null;
        antall = 0;
        endringer++;
    }

    @Override
    public String toString() {
        StringJoiner s = new StringJoiner(", ", "[", "]");

        Node<T> p = hode;
        while (p != null) {
            s.add(p.verdi.toString());
            p = p.neste;
        }
        return s.toString();
    }

    public String omvendtString() {
        StringJoiner s = new StringJoiner(", ", "[", "]");

        Node<T> p = hale;

        while (p != null) {
            if (p.verdi != null) {
                s.add(p.verdi.toString());
            }
            p = p.forrige;
        }
        return s.toString();
    }

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
        for (int i = 1; i < liste.antall(); i++) {
            T verdi = liste.hent(i);
            int j = i - 1;

            for (; j >= 0 && c.compare(verdi, liste.hent(j)) < 0; j--) {
                liste.oppdater(j + 1, liste.hent(j));
            }

            liste.oppdater(j + 1, verdi);
        }
    }
    
    // Ulf sorter
    // finn minste, fjern og legg inn bak. Finn neste, men ikke gjennom hele tabellen. 
    // Teller holder styr på hvor stor del av tabellen vi søker i

    @Override
    public Iterator<T> iterator() {
        return new DobbeltLenketListeIterator();
    }

    public Iterator<T> iterator(int indeks) {
        indeksKontroll(indeks, false);
        return new DobbeltLenketListeIterator(indeks);
    }

    private class DobbeltLenketListeIterator implements Iterator<T> {

        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;

        private DobbeltLenketListeIterator() {
            denne = hode;     // denne starter på den første i listen
            fjernOK = false;  // blir sann når next() kalles
            iteratorendringer = endringer;  // teller endringer
        }

        private DobbeltLenketListeIterator(int indeks) {
            denne = finnNode(indeks);
            fjernOK = false;
            iteratorendringer = endringer;
        }

        @Override
        public boolean hasNext() {
            return denne != null;  // denne koden skal ikke endres!
        }

        @Override
        public T next() {
            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException("Listen er endret!");
            }
            if (!hasNext()) {
                throw new NoSuchElementException("Ikke flere verdier!");
            }
            fjernOK = true;
            T gammelverdi = denne.verdi;
            denne = denne.neste;
            return gammelverdi;
        }

        @Override
        public void remove() {
            if (!fjernOK) {
                throw new IllegalStateException("Ulovlig tilstand");
            }

            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException("Listen er endret!");
            }

            fjernOK = false;

            if (antall == 1) {
                hode = hale = null;
            } else if (denne == null) {
                hale = hale.forrige;
                hale.neste.forrige = null;
                hale.neste = null;
            } else if (denne.forrige == hode) {
                hode.neste = null;
                hode = denne;
                hode.forrige = null;
            } else {
                denne.forrige = denne.forrige.forrige;
                denne.forrige.neste.neste = null;
                denne.forrige.neste.forrige = null;
                denne.forrige.neste = denne;
            }
            antall--;
            endringer++;
            iteratorendringer++;
        }

    } // DobbeltLenketListeIterator  

} // DobbeltLenketListe  
