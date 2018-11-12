import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArkaiveClass {
	@SerializedName("myClass")
	@Expose
	String myClass;
	@SerializedName("couseCode")
	@Expose
	String couseCode;
}