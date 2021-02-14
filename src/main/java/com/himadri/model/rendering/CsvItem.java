package com.himadri.model.rendering;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CsvItem {
    @CsvBindByName(required = true)
    private String cikkszam;
    @CsvBindByName
    private String dtpmegnevezes;
    @CsvBindByName
    private String leiras;
    @CsvBindByName
    private String cikkfajta;
    @CsvBindByName(required = true)
    private String cikkcsoportnev;
    @CsvBindByName
    private String gyarto;
    @CsvBindByName(required = true)
    private String kiskerar;
    @CsvBindByName
    private String nagykerar;
    @CsvBindByName
    private String m1;
    @CsvBindByName
    private String m2;
    @CsvBindByName
    private String m3;
    @CsvBindByName
    private String me;
    @CsvBindByName
    private String kepnev;
    @CsvBindByName
    private String targymutato;
    @CsvBindByName
    private String webkepnev;
    @CsvBindByName
    private String nagykep;
    @CsvBindByName
    private String uj;
    @CsvBindByName
    private String ertmenny;
    @CsvBindByName
    private String ujoldalon;
    @CsvBindByName
    private String szin;
    @CsvBindByName
    private String gyujto;
    @CsvBindByName
    private String karton;
}
