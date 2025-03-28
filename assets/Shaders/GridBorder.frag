#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_position;
varying vec3 v_mapSize;
varying vec3 v_cameraPosition;

uniform float u_gridSize;
uniform float u_gridThickness;
uniform vec4 u_gridColor;

void main() {
    // Calculate distance from edges
    vec3 distFromEdge = min(v_position, v_mapSize - v_position);
    float minDist = min(min(distFromEdge.x, distFromEdge.y), distFromEdge.z);

    // Calculate grid lines
    vec3 grid = mod(v_position, u_gridSize);
    grid = min(grid, u_gridSize - grid);

    // Determine if we're on a grid line
    float gridLine = min(min(grid.x, grid.y), grid.z);

    // Adjust alpha based on distance from camera (fade out when far away)
    float dist = length(v_position - v_cameraPosition);
    float alpha = u_gridColor.a * (1.0 - min(1.0, dist / (v_mapSize.x * 1.5)));

    // Highlight edges more than grid lines
    if (minDist < u_gridThickness * 2.0) {
        // We're on an edge
        gl_FragColor = vec4(u_gridColor.rgb, alpha);
    } else if (gridLine < u_gridThickness) {
        // We're on a grid line
        gl_FragColor = vec4(u_gridColor.rgb, alpha * 0.7);
    } else {
        // We're in empty space - fully transparent
        discard;
    }
}
