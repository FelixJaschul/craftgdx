attribute vec3 a_position;

uniform mat4 u_projTrans;
uniform vec3 u_mapSize;
uniform vec3 u_cameraPosition;

varying vec3 v_position;
varying vec3 v_mapSize;
varying vec3 v_cameraPosition;

void main() {
    // Pass the position to the fragment shader
    v_position = a_position;
    v_mapSize = u_mapSize;
    v_cameraPosition = u_cameraPosition;

    // Transform the vertex by the projection and view matrices
    gl_Position = u_projTrans * vec4(a_position, 1.0);
}
