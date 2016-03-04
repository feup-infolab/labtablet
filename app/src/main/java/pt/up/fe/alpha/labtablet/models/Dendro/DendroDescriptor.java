
package pt.up.fe.alpha.labtablet.models.Dendro;


public class DendroDescriptor {
   	private String comment;
   	private String control;
   	private String label;
   	private String last_use;
   	private String ontology;
   	private String prefix;
   	private String prefixedForm;
   	private String recently_used_count;
   	private Recommendation_types recommendation_types;
   	private Number score;
   	private String shortName;
   	private Number type;
   	private String uri;

 	public String getComment(){
		return this.comment;
	}
	public void setComment(String comment){
		this.comment = comment;
	}
 	public String getControl(){
		return this.control;
	}
	public void setControl(String control){
		this.control = control;
	}
 	public String getLabel(){
		return this.label;
	}
	public void setLabel(String label){
		this.label = label;
	}
 	public String getLast_use(){
		return this.last_use;
	}
	public void setLast_use(String last_use){
		this.last_use = last_use;
	}
 	public String getOntology(){
		return this.ontology;
	}
	public void setOntology(String ontology){
		this.ontology = ontology;
	}
 	public String getPrefix(){
		return this.prefix;
	}
	public void setPrefix(String prefix){
		this.prefix = prefix;
	}
 	public String getPrefixedForm(){
		return this.prefixedForm;
	}
	public void setPrefixedForm(String prefixedForm){
		this.prefixedForm = prefixedForm;
	}
 	public String getRecently_used_count(){
		return this.recently_used_count;
	}
	public void setRecently_used_count(String recently_used_count){
		this.recently_used_count = recently_used_count;
	}
 	public Recommendation_types getRecommendation_types(){
		return this.recommendation_types;
	}
	public void setRecommendation_types(Recommendation_types recommendation_types){
		this.recommendation_types = recommendation_types;
	}
 	public Number getScore(){
		return this.score;
	}
	public void setScore(Number score){
		this.score = score;
	}
 	public String getShortName(){
		return this.shortName;
	}
	public void setShortName(String shortName){
		this.shortName = shortName;
	}
 	public Number getType(){
		return this.type;
	}
	public void setType(Number type){
		this.type = type;
	}
 	public String getUri(){
		return this.uri;
	}
	public void setUri(String uri){
		this.uri = uri;
	}
}
