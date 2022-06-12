package zendo.games.gltfsandbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture image;

	private SceneManager sceneManager;
	private SceneAsset sceneAsset;
	private Scene scene;
	private PerspectiveCamera camera;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Cubemap diffuseCubemap;
	private Texture brdfLUT;
	private SceneSkybox skybox;
	private DirectionalLightEx light;
	private float time;

//	private float d = 0.02f;
	private float d = 2f;

	@Override
	public void create() {
		batch = new SpriteBatch();
		image = new Texture("libgdx.png");

		sceneAsset = new GLBLoader().load(Gdx.files.internal("models/block.glb"));
		scene = new Scene(sceneAsset.scene);
		sceneManager = new SceneManager();
		sceneManager.addScene(scene);

		camera = new PerspectiveCamera(66f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = d / 1000f;
		camera.far = d * 4;
		sceneManager.setCamera(camera);

		light = new DirectionalLightEx();
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		sceneManager.environment.add(light);

		var iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		specularCubemap = iblBuilder.buildRadianceMap(10);
		diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		iblBuilder.dispose();

		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

		sceneManager.setAmbientLight(1f);
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

		skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);
	}

	@Override
	public void dispose() {
		batch.dispose();
		image.dispose();
		sceneManager.dispose();
		sceneAsset.dispose();
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		brdfLUT.dispose();
		skybox.dispose();
	}

	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
	}

	@Override
	public void render() {
		var delta = Gdx.graphics.getDeltaTime();
		time += delta;

		camera.position.setFromSpherical(MathUtils.PI / 4, time * 0.3f).scl(d);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();

		ScreenUtils.clear(Color.SKY, true);
		sceneManager.update(delta);
		sceneManager.render();
	}

}