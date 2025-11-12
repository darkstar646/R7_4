import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * グラフィックスを簡単に表示するウィンドウクラス
 * <p>サブクラスに off を公開し、Graphics2D にすることで、描画機能を充実させた。
 * 描画座標の原点を表示するように、全体をシフトすることも可能。
 *
 * 2024.11.06 自動保存の画像が大きすぎるので、ウィンドウに合わせてクリップする。
 * 2023.10.11 WindowManager によっては setSize() の微小サイズ変更を乱発する。
 *            オフスクリーンを少し大きめにして、clear() の隙に描画されることを防ぐ。
 * 2022.12.21 マウスでウインドウの大きさを変更すると、描画領域の大きさも追従する。
 * 2022.10.05 setSize() でも全体をクリアしないようにした。
 * 
 * @author TSUCHIMURA Nobuyuki
 * @version 2024.11.06
 */
public class MyFrame2 extends Frame implements Runnable {
	private static final long serialVersionUID = -4998025212974662457L;
	/** オフスクリーンの幅と高さ（ウィンドウよりも大きい可能性がある） */
	private int width = -1, height = -1;
	/** オフスクリーンイメージ */
	private BufferedImage offScreen;
	/** オフスクリーンイメージの Graphics(2D) コンテキスト */
	Graphics2D off;
	/** fillRect 等で用いる描画色 */
	private Color fgColor = Color.BLACK;
	/** clear() で用いる背景色 */
	Color bgColor = Color.WHITE;
	/** 自動保存フラグ（"screenshots"フォルダを作っておくこと） */
	boolean autoSave = false;
	/** 原点を表示するよう、全体をシフトする */
	boolean viewOrigin = false;
	private Insets inset = new Insets(0, 0, 0, 0);//2021.11.15
	/** アニメーション用のスレッド */
	private Thread thread = null;

	/** ディフォルトサイズのウィンドウを作成し、表示する。 */
	public MyFrame2() { this(400, 400);}

	/** 指定したサイズのウィンドウを作成し、表示する。 */
	public MyFrame2(int width, int height) {
		setSize(width, height);
		setTitle(getClass().getName());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				System.exit(1);
			}
		});
		addComponentListener(new ComponentAdapter() { // 2022.12.21
			@Override
			public void componentResized(ComponentEvent e) {
//				System.out.print("resized, ");
				Dimension d = getSize();
				setSize(d.width, d.height); // this.setSize(d)がないので
			}
		});
		setVisible(true);
	}

	/**
	 * ウィンドウサイズを変更する。
	 * <p>画面全体まではクリアされないが、拡大した部分はクリアされる。
	 */
	@Override
	public void setSize(int width, int height) {
//		System.out.println(width + " x " + height);
		BufferedImage save = offScreen;
		super.setSize(width + inset.right + inset.left,
				height + inset.top + inset.bottom);
		if (this.width < width || this.height < height) {
			// 過去の最大値をとる 2022.12.21 少し大きめに 2023.10.11
			if (this.width  < width)  this.width  = width  + 100;
			if (this.height < height) this.height = height + 100;
			offScreen = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
			off = (Graphics2D) offScreen.getGraphics();
			off.setColor(fgColor);
			clear(); // off の背景色がプラットフォーム依存なので、明示的に塗りつぶす
			if (save != null) off.drawImage(save, 0, 0,  null); // 以前の描画内容を引き継ぐ
//			System.out.println("this size = " + this.width + " x " + this.height);
		}
		//repaint(); 再表示まではしなくてよい
	}

	/** 最初に paint() が呼ばれたときに、別スレッドで run() を実行する。 */
	@Override
	public void paint(Graphics g) {
//		System.out.println("paint");
		if (thread == null) {
			final Runnable outer = this; // inner classから外部のrun()を呼び出すため
			thread = new Thread() {
				public void run() {
//					System.out.println("outer.run");
					outer.run(); // MyFrame2（をextendsしたクラス）のrun()を実行
					repaint();   // 最後に必ずupdate()で描かせる
				}
			};
			thread.start();
		} else {
			update(g);
		}
	}

	/** repaint() によって呼び出され、オフスクリーンイメージを表示する。 */
	@Override
	public void update(Graphics g) {
//		System.out.println("update");
		if (viewOrigin) {
			Insets in = getInsets(); // ウインドウ周囲の境界の大きさ
			if (!inset.equals(in)) {
//				System.out.println("super.setSize");
				inset = in;
				super.setSize(width + inset.right + inset.left,
						height + inset.top + inset.bottom);
			}
		}
		// オフスクリーンイメージを表示する。
		g.drawImage(offScreen, inset.left, inset.top, null);
	}

	/** 描画領域全体を背景色で塗りつぶす。 */
	public void clear() {
		off.setBackground(bgColor); // bgColorがpublicなので、毎回セットしなおす
		off.clearRect(0, 0, width, height);
	}

	/**
	 * 描画色を指定する。
	 * 
	 * @param red   赤色成分(0..255)
	 * @param green 緑色成分(0..255)
	 * @param blue  青色成分(0..255)
	 */
	public void setColor(int red, int green, int blue) {
		red   = Math.min(255, Math.max(0, red));
		green = Math.min(255, Math.max(0, green));
		blue  = Math.min(255, Math.max(0, blue));
		fgColor = new Color(red, green, blue);
		off.setColor(fgColor);
	}

	/**
	 * 長方形を描画する。色は setColor() で事前に指定しておく。
	 * 
	 * @param x 左端のx座標
	 * @param y 上端のy座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public void fillRect(int x, int y, int w, int h) {
		off.fillRect(x, y, w, h);
	}
	public void fillRect(double x, double y, double w, double h) {
		off.fillRect((int) x, (int) y, (int) w, (int) h);
	}

	/**
	 * 楕円を描画する。色は setColor() で事前に指定しておく。
	 * 
	 * @param x 左端のx座標
	 * @param y 上端のy座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public void fillOval(int x, int y, int w, int h) {
		off.fillOval(x, y, w, h);
	}
	public void fillOval(double x, double y, double w, double h) {
		off.fillOval((int) x, (int) y, (int) w, (int) h);
	}

	/**
	 * 文字列を描画する。色は setColor() で事前に指定しておく。
	 * 
	 * @param str 描画される文字列
	 * @param x 文字列の左端のx座標
	 * @param y 文字列のベースラインのy座標
	 * @param size 大きさ（ポイント数）
	 */
	public void drawString(String str, int x, int y, int size) {
		off.setFont(new Font("Monospaced", Font.PLAIN, size));
		off.drawString(str, x, y);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		off.drawLine(x1, y1, x2, y2);
	}
	public void drawLine(double x1, double y1, double x2, double y2) {
		off.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
	}
	
	public void drawImage(Image img, int x, int y) {
		off.drawImage(img, x, y, this);
	}

	public void drawImage(Image img, int x, int y, int w, int h) {
		off.drawImage(img, x, y, w, h, this);
	}

	public Image readImage(String pathname) {
		try {
			return ImageIO.read(new File(pathname));
		} catch (IOException e) {
			System.err.println(pathname + " の読み取りに失敗しました。");
			return null;
		}
	}

	public void saveImage(File output) throws IOException {
		Dimension d = getSize(); // 2024.11.06 ウィンドウの大きさに合わせてクリップする
		ImageIO.write(offScreen.getSubimage(0, 0, d.width, d.height), "png", output);
	}

	/** 自動保存画像の連番 */
	private static volatile int num = 0;

	/**
	 * 一定時間待つ。（その間に再描画と自動保存を行う）
	 * 
	 * @param sec 待ち時間(秒)
	 */
	public synchronized void sleep(double sec) {
		repaint(); // これで update(g) が呼び出される
		try {
			Thread.sleep((int) (sec * 1000));
			if (autoSave) {
				String pathfile = "screenshots" + File.separator +
						getClass().getName() + String.format("_%04d.png", ++num);
				saveImage(new File(pathfile));
			}
		} catch (IOException e) {
			System.err.println("'screenshots' フォルダが必要です。");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** このrun()をオーバライドして、画面の描画命令を実装する。 */
	public void run() {
	}
}
