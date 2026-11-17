package com.LuccasAlmeida.marketlists;

import java.util.ArrayList;
import java.util.List;

public class CategoryData {

    public static List<Categoria> getCategoriasPadrao() {
        List<Categoria> lista = new ArrayList<>();

        lista.add(new Categoria(1, "Bebidas", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699458/bebidas.jpg"));
        lista.add(new Categoria(2, "Carnes", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699458/carnes.jpg"));
        lista.add(new Categoria(3, "Frios", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792172754/frios.jpg"));
        lista.add(new Categoria(4, "Guloseimas", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699471/guloseimas.jpg"));
        lista.add(new Categoria(5, "Hortifruti", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792172762/hortifr%C3%BAti.jpg"));
        lista.add(new Categoria(6, "Limpeza e Higiene", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699479/limpeza_higiene.jpg"));
        lista.add(new Categoria(7, "Mercearia", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699479/mercearia.jpg"));
        lista.add(new Categoria(8, "Padaria", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792172763/Padaria.jpg"));
        lista.add(new Categoria(9, "Personalizado", "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792172633/samples/ecommerce/accessories-bag.jpg"));

        return lista;
    }
}