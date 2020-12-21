package com.bram.belajarosmdroid.API;

import androidx.annotation.NonNull;

public class Shift {
    Integer jamkerja_id;
    Integer hcr_group_id;
    Integer shift;
    String jmasuk;
    String jpulang;
    Integer flexi;
    Integer jefektif;
    String ket;
    Boolean jamkerja_aktif;

    public Shift(Integer jamkerja_id, Integer hcr_group_id, Integer shift, String jmasuk, String jpulang,
                 Integer flexi, Integer jefektif, String ket, Boolean jamkerja_aktif) {
        this.jamkerja_id = jamkerja_id;
        this.hcr_group_id = hcr_group_id;
        this.shift = shift;
        this.jmasuk = jmasuk;
        this.jpulang = jpulang;
        this.flexi = flexi;
        this.jefektif = jefektif;
        this.ket = ket;
        this.jamkerja_aktif = jamkerja_aktif;
    }
    public Shift(){

    }

    public Integer getJamkerja_id() {
        return jamkerja_id;
    }

    public void setJamkerja_id(Integer jamkerja_id) {
        this.jamkerja_id = jamkerja_id;
    }

    public Integer getHcr_group_id() {
        return hcr_group_id;
    }

    public void setHcr_group_id(Integer hcr_group_id) {
        this.hcr_group_id = hcr_group_id;
    }

    public Integer getShift() {
        return shift;
    }

    public void setShift(Integer shift) {
        this.shift = shift;
    }

    public String getJmasuk() {
        return jmasuk;
    }

    public void setJmasuk(String jmasuk) {
        this.jmasuk = jmasuk;
    }

    public String getJpulang() {
        return jpulang;
    }

    public void setJpulang(String jpulang) {
        this.jpulang = jpulang;
    }

    public Integer getFlexi() {
        return flexi;
    }

    public void setFlexi(Integer flexi) {
        this.flexi = flexi;
    }

    public Integer getJefektif() {
        return jefektif;
    }

    public void setJefektif(Integer jefektif) {
        this.jefektif = jefektif;
    }

    public String getKet() {
        return ket;
    }

    public void setKet(String ket) {
        this.ket = ket;
    }

    public Boolean getJamkerja_aktif() {
        return jamkerja_aktif;
    }

    public void setJamkerja_aktif(Boolean jamkerja_aktif) {
        this.jamkerja_aktif = jamkerja_aktif;
    }

    @NonNull
    @Override
    public String toString() {
        return ket+" - ("+jmasuk+"-"+jpulang+")";
    }
}
