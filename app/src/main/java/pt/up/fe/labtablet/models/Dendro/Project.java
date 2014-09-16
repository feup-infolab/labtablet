package pt.up.fe.labtablet.models.Dendro;

import pt.up.fe.labtablet.models.Dendro.Ontologies.Achem;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Dcb;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Dcterms;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Ddr;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Foaf;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Nfo;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Nie;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Rdf;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Research;

public class Project {
    private Achem achem;
    private Dcb dcb;
    private Dcterms dcterms;
    private Ddr ddr;
    private Foaf foaf;
    private Nfo nfo;
    private Nie nie;
    private Rdf rdf;
    private Research research;
    private String uri;

    public Project() {
    }

    public Achem getAchem() {
        return this.achem;
    }

    public void setAchem(Achem achem) {
        this.achem = achem;
    }

    public Dcb getDcb() {
        return this.dcb;
    }

    public void setDcb(Dcb dcb) {
        this.dcb = dcb;
    }

    public Dcterms getDcterms() {
        return this.dcterms;
    }

    public void setDcterms(Dcterms dcterms) {
        this.dcterms = dcterms;
    }

    public Ddr getDdr() {
        return this.ddr;
    }

    public void setDdr(Ddr ddr) {
        this.ddr = ddr;
    }

    public Foaf getFoaf() {
        return this.foaf;
    }

    public void setFoaf(Foaf foaf) {
        this.foaf = foaf;
    }

    public Nfo getNfo() {
        return this.nfo;
    }

    public void setNfo(Nfo nfo) {
        this.nfo = nfo;
    }

    public Nie getNie() {
        return this.nie;
    }

    public void setNie(Nie nie) {
        this.nie = nie;
    }

    public Rdf getRdf() {
        return this.rdf;
    }

    public void setRdf(Rdf rdf) {
        this.rdf = rdf;
    }

    public Research getResearch() {
        return this.research;
    }

    public void setResearch(Research research) {
        this.research = research;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}