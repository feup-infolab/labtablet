package pt.up.fe.labtablet.models.Dendro;

/**
 * Created by ricardo on 08-05-2014.
 */
import java.util.ArrayList;
import java.util.List;

import pt.up.fe.labtablet.models.Dendro.Ontologies.Achem;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Dcb;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Dcterms;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Ddr;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Foaf;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Nfo;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Nie;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Rdf;
import pt.up.fe.labtablet.models.Dendro.Ontologies.Research;

public class ProjectListResponse{
    private ArrayList<Project> projects;

    public ArrayList<Project> getProjects(){
        return this.projects;
    }
    public void setProjects(ArrayList<Project> projects){
        this.projects = projects;
    }


}
