package com.himadri.model.rendering;

import com.opencsv.bean.CsvBindByName;

public class Item {
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

    public Item() {
    }

    public String getCikkszam() {
        return cikkszam;
    }

    public void setCikkszam(String cikkszam) {
        this.cikkszam = cikkszam;
    }

    public String getDtpmegnevezes() {
        return dtpmegnevezes;
    }

    public void setDtpmegnevezes(String dtpmegnevezes) {
        this.dtpmegnevezes = dtpmegnevezes;
    }

    public String getLeiras() {
        return leiras;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }

    public String getCikkfajta() {
        return cikkfajta;
    }

    public void setCikkfajta(String cikkfajta) {
        this.cikkfajta = cikkfajta;
    }

    public String getCikkcsoportnev() {
        return cikkcsoportnev;
    }

    public void setCikkcsoportnev(String cikkcsoportnev) {
        this.cikkcsoportnev = cikkcsoportnev;
    }

    public String getGyarto() {
        return gyarto;
    }

    public void setGyarto(String gyarto) {
        this.gyarto = gyarto;
    }

    public String getKiskerar() {
        return kiskerar;
    }

    public void setKiskerar(String kiskerar) {
        this.kiskerar = kiskerar;
    }

    public String getNagykerar() {
        return nagykerar;
    }

    public void setNagykerar(String nagykerar) {
        this.nagykerar = nagykerar;
    }

    public String getM1() {
        return m1;
    }

    public void setM1(String m1) {
        this.m1 = m1;
    }

    public String getM2() {
        return m2;
    }

    public void setM2(String m2) {
        this.m2 = m2;
    }

    public String getM3() {
        return m3;
    }

    public void setM3(String m3) {
        this.m3 = m3;
    }

    public String getMe() {
        return me;
    }

    public void setMe(String me) {
        this.me = me;
    }

    public String getKepnev() {
        return kepnev;
    }

    public void setKepnev(String kepnev) {
        this.kepnev = kepnev;
    }

    public String getTargymutato() {
        return targymutato;
    }

    public void setTargymutato(String targymutato) {
        this.targymutato = targymutato;
    }

    public String getWebkepnev() {
        return webkepnev;
    }

    public void setWebkepnev(String webkepnev) {
        this.webkepnev = webkepnev;
    }
}
