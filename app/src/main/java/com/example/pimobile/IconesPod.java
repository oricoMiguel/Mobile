package com.example.pimobile;

// Catálogo dos ícones que um skill pod pode ter.
// Cada ícone tem uma cor fixa (0 = roxo, 1 = verde, 2 = laranja), a mesma do fundo do card.
public class IconesPod {

    public static class Item {
        public final String chave;   // o que fica gravado no Firestore
        public final String rotulo;  // o que aparece no formulário
        public final int res;
        public final int cor;

        Item(String chave, String rotulo, int res, int cor) {
            this.chave = chave;
            this.rotulo = rotulo;
            this.res = res;
            this.cor = cor;
        }
    }

    public static final Item[] TODOS = {
            new Item("code", "Código", R.drawable.ic_code, 0),
            new Item("palette", "Design", R.drawable.ic_palette, 1),
            new Item("megaphone", "Marketing", R.drawable.ic_megaphone, 2),
            new Item("dados", "Dados", R.drawable.ic_pod_dados, 0),
            new Item("video", "Vídeo e foto", R.drawable.ic_pod_video, 2),
            new Item("escrita", "Escrita", R.drawable.ic_pod_escrita, 1),
            new Item("suporte", "Suporte", R.drawable.ic_pod_suporte, 0),
            new Item("educacao", "Educação", R.drawable.ic_pod_educacao, 1),
            new Item("eventos", "Eventos", R.drawable.ic_pod_eventos, 2),
    };

    // Chave desconhecida ou nula cai no ícone de código, como já era antes
    public static Item porChave(String chave) {
        for (Item item : TODOS) {
            if (item.chave.equals(chave)) return item;
        }
        return TODOS[0];
    }
}
