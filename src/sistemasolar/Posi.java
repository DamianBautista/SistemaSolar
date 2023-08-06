
package sistemasolar;

/**
 * 
 * @author DamianBautista
 * 
 */
import javax.media.j3d.Alpha;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Node;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

/** 
 * Clase Posi: Contiene métodos para realizar rotación y traslación de nodos en un sistema solar.
 * Estos métodos se utilizan para animar y posicionar los planetas y otros objetos del sistema solar.
 */
public class Posi{
    
    /**
     * Método para aplicar una rotación a un nodo en un sistema solar.
     * @param node El nodo que se va a rotar.
     * @param alpha El objeto Alpha que controla la duración y velocidad de la rotación.
     * @return TransformGroup que contiene el nodo rotado con interpolación de rotación.
     */
    public static TransformGroup rotate(Node node, Alpha alpha){
        TransformGroup xformGroup = new TransformGroup();
        xformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        // Creamos un interpolador de rotación para animar el nodo con el objeto Alpha.
        RotationInterpolator interpolator = new RotationInterpolator(alpha, xformGroup);
        
        // Definimos el límite del interpolador para la esfera delimitadora con radio 1.0 y centro en (0,0,0).
        interpolator.setSchedulingBounds(new BoundingSphere(new Point3d(0.0,0.0,0.0),1.0));
        
        // Agregamos el interpolador y el nodo al grupo de transformación.
        xformGroup.addChild(interpolator);
        xformGroup.addChild(node);
        return xformGroup;
    }
    
    /**
     * Método para aplicar una traslación a un nodo en un sistema solar.
     * @param node El nodo que se va a trasladar.
     * @param vector El vector de traslación que indica la dirección y magnitud del movimiento.
     * @return TransformGroup que contiene el nodo trasladado a la posición indicada por el vector.
     */
    public static TransformGroup translate(Node node, Vector3f vector){
        Transform3D transform3D = new Transform3D();
        
        // Configuramos la transformación para realizar la traslación según el vector especificado.
        transform3D.setTranslation(vector);
        
        // Creamos un grupo de transformación con la matriz de traslación aplicada.
        TransformGroup transformGroup = new TransformGroup(transform3D);
        
        // Agregamos el nodo al grupo de transformación para que quede en la posición trasladada.
        transformGroup.addChild(node);
        
        return transformGroup;
    }
}
