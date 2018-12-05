import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.PMVMatrix;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;

public class StartRendererPP extends GLCanvas implements GLEventListener {

    private static final long serialVersionUID = 1L;

    final String shaderPath = ".\\resources\\";
    final String vertexShaderFileName = "BlinnPhongPointTex.vert";
    final String fragmentShaderFileName = "BlinnPhongPointTex.frag";
    private ShaderProgram shaderProgram;

    private LightSource light0;
    private Material material0;
    private LoadTexture texture;

    // Pointers for data transfer and handling on GPU
    private int[] vaoName;  // Name of vertex array object
    private int[] vboName;	// Name of vertex buffer object
    private int[] iboName;	// Name of index buffer object

    InteractionHandler interactionHandler; //Object for handling keyboard and mouse interaction
    PMVMatrix pmvMatrix; // Projection model view matrix tool

    public StartRendererPP(GLCapabilities capabilities) {
        super(capabilities);
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
    }

    private void createAndRegisterInteractionHandler() {
        interactionHandler = new InteractionHandler();
        this.addKeyListener(interactionHandler);
        this.addMouseListener(interactionHandler);
        this.addMouseMotionListener(interactionHandler);
        this.addMouseWheelListener(interactionHandler);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        // Verify if VBO-Support is available
        if(!gl.isExtensionAvailable("GL_ARB_vertex_buffer_object"))
            System.out.println("Error: VBO support is missing");
        else
            System.out.println("VBO support is available");

        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath,
                vertexShaderFileName, fragmentShaderFileName);

        int noOfObjects = 6;
        vaoName = new int[noOfObjects];
        for (int i = 0; i < vaoName.length; i++) {
            gl.glGenVertexArrays(noOfObjects, vaoName, 0);
            if (vaoName[i] < 1)
                System.err.println("Error allocating vertex array object (VAO).");
            gl.glBindVertexArray(vaoName[i]);
        }

        vboName = new int[noOfObjects];
        for (int i = 0; i < vboName.length; i++) {
            gl.glGenBuffers(noOfObjects, vboName, 0);
            if (vboName[i] < 1)
                System.err.println("Error allocating vertex buffer object (VBO).");
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[i]);
        }

        iboName = new int[noOfObjects];
        for (int i = 0; i < iboName.length; i++) {
            gl.glGenBuffers(noOfObjects, iboName, 0);
            if (iboName[i] < 1)
                System.err.println("Error allocating index buffer object.");
        }

        //lightparameters
        float[] lightPosition = {0.0f, 3.0f, 3.0f, 1.0f};
        float[] lightAmbientColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightDiffuseColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] lightSpecularColor = {1.0f, 1.0f, 1.0f, 1.0f};
        light0 = new LightSource(lightPosition, lightAmbientColor,
                lightDiffuseColor, lightSpecularColor);

        pmvMatrix = new PMVMatrix();
        interactionHandler.setEyeZ(0.5f);

        initMainTable(gl);
        initTableLegVR(gl);
        initTableLegVL(gl);
        initTableLegHL(gl);
        initTableLegHR(gl);
        initCard(gl);

        //gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    private void initMainTable(GL3 gl) {
        gl.glBindVertexArray(vaoName[0]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.5f, 0.5f, 0.5f};
        float[] cubeVertices = DrawTable.makeBoxVertices(0.8f,0.05f,0.6f, color0);
        int[] tableIndices = DrawTable.makeBoxIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[0]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);

        float[] matEmission = {0.0f, 0.0f, 0.0f, 1.0f};
        float[] matAmbient =  {0.2f, 0.2f, 0.2f, 1.0f};
        float[] matDiffuse =  {0.5f, 0.5f, 0.5f, 1.0f};
        float[] matSpecular = {0.7f, 0.7f, 0.7f, 1.0f};
        float matShininess = 200.0f;

        material0 = new Material(matEmission, matAmbient, matDiffuse, matSpecular, matShininess);

        //texture

        texture = new LoadTexture();
        texture.loadTexture(gl, "resources/holz-struktur.jpg");



    }

    private void displayMainTable(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glUniform4fv(2, 1, light0.getPosition(), 0);
        gl.glUniform4fv(3, 1, light0.getAmbient(), 0);
        gl.glUniform4fv(4, 1, light0.getDiffuse(), 0);
        gl.glUniform4fv(5, 1, light0.getSpecular(), 0);

        gl.glUniform4fv(6, 1, material0.getEmission(), 0);
        gl.glUniform4fv(7, 1, material0.getAmbient(), 0);
        gl.glUniform4fv(8, 1, material0.getDiffuse(), 0);
        gl.glUniform4fv(9, 1, material0.getSpecular(), 0);
        gl.glUniform1f(10, material0.getShininess());

        gl.glBindVertexArray(vaoName[0]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawTable.noOfIndicesForBox(), GL.GL_UNSIGNED_INT, 0);
    }

    private void initTableLegVR(GL3 gl) {
        gl.glBindVertexArray(vaoName[1]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.5f, 0.5f, 0.5f};
        float[] cubeVertices = DrawTable.tableLegVRVerticices(color0);
        int[] tableIndices = DrawTable.makeVRLegIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[1]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[1]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);
    }

    private void displayTableLegVR(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glBindVertexArray(vaoName[1]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawTable.noOfIndicesForVRLeg(), GL.GL_UNSIGNED_INT, 0);
    }

    private void initTableLegVL(GL3 gl) {
        gl.glBindVertexArray(vaoName[2]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.5f, 0.5f, 0.5f};
        float[] cubeVertices = DrawTable.tableLegVLVerticices(color0);
        int[] tableIndices = DrawTable.makeVLLegIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[2]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[2]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);
    }

    private void displayTableLegVL(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glBindVertexArray(vaoName[2]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawTable.noOfIndicesForVLLeg(), GL.GL_UNSIGNED_INT, 0);
    }

    private void initTableLegHL(GL3 gl) {
        gl.glBindVertexArray(vaoName[3]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.5f, 0.5f, 0.5f};
        float[] cubeVertices = DrawTable.tableLegHLVerticices(color0);
        int[] tableIndices = DrawTable.makeHLLegIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[3]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[3]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);
    }

    private void displayTableLegHL(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glBindVertexArray(vaoName[3]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawTable.noOfIndicesForHLLeg(), GL.GL_UNSIGNED_INT, 0);
    }

    private void initTableLegHR(GL3 gl) {
        gl.glBindVertexArray(vaoName[4]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.5f, 0.5f, 0.5f};
        float[] cubeVertices = DrawTable.tableLegHRVerticices(color0);
        int[] tableIndices = DrawTable.makeHRLegIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[4]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[4]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);
    }

    private void displayTableLegHR(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glBindVertexArray(vaoName[4]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawTable.noOfIndicesForHRLeg(), GL.GL_UNSIGNED_INT, 0);
    }

    private void initCard(GL3 gl) {
        gl.glBindVertexArray(vaoName[5]);
        shaderProgram = new ShaderProgram(gl);
        shaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        float[] color0 = {0.0f, 0.0f, 0.0f};
        float[] cubeVertices = DrawCard.makeCardVertices(color0);
        int[] tableIndices = DrawCard.makeCardIndicesForTriangleStrip();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[5]);

        gl.glBufferData(GL.GL_ARRAY_BUFFER, cubeVertices.length * 4,
                FloatBuffer.wrap(cubeVertices), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[5]);

        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, tableIndices.length * 4,
                IntBuffer.wrap(tableIndices), GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 11*4, 0);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 11*4, 3*4);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, 11*4, 6*4);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, 11*4, 9*4);

        //texture
        //texture = new LoadTexture();
        //texture.loadTexture(gl, "resources/Karte.JPG");


    }

    private void displayCard(GL3 gl) {
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        gl.glBindVertexArray(vaoName[5]);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, DrawCard.noOfIndicesForCard(), GL.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        gl.glUseProgram(shaderProgram.getShaderProgramID());

        System.out.println("Camera: z = " + interactionHandler.getEyeZ() + ", " +
                "x-Rot: " + interactionHandler.getAngleXaxis() +
                ", y-Rot: " + interactionHandler.getAngleYaxis() +
                ", x-Translation: " + interactionHandler.getxPosition()+
                ", y-Translation: " + interactionHandler.getyPosition());// definition of translation of model (Model/Object Coordinates --> World Coordinates)

        pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluLookAt(0f, 2f, interactionHandler.getEyeZ(),
                            0f, 0f, 0f,
                            0f, 1.0f, 0f);
        pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

        //display Table
        pmvMatrix.glPushMatrix();
        displayMainTable(gl);
        displayTableLegVR(gl);
        displayTableLegVL(gl);
        displayTableLegHL(gl);
        displayTableLegHR(gl);
        pmvMatrix.glPopMatrix();

        pmvMatrix.glPushMatrix();
        displayCard(gl);
        pmvMatrix.glPopMatrix();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();

        pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluPerspective(45f, (float) width/ (float) height, 0.1f, 100f);
    }


    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Deleting allocated objects, incl. shader program.");
        GL3 gl = drawable.getGL().getGL3();

        // Detach and delete shader program
        gl.glUseProgram(0);
        shaderProgram.deleteShaderProgram();

        // deactivate VAO and VBO
        gl.glBindVertexArray(0);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);

        System.exit(0);
    }
}
