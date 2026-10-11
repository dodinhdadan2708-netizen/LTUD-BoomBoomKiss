package boomboomkiss.model;

import boomboomkiss.repository.ThemeRepository;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Exercises the real home actions without desktop input or writing the user's theme. */
public final class HomeTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Main app = null;
            try {
                Theme theme = new Theme();
                int[] writes = {0};
                Game game = new Game(new ThemeRepository() {
                    public Theme load() { return theme; }
                    public void save(Theme value) { writes[0]++; }
                });
                app = new Main(game);
                Path previews=Paths.get("boomboomkiss/build/previews"); Files.createDirectories(previews);
                for (Dimension size : new Dimension[] {new Dimension(480,850),new Dimension(400,660)}) {
                    app.setSize(size); layout(app);
                    check(find(app.getContentPane(),JScrollPane.class).isEmpty(),"home never scrolls");
                    check(find(app.getContentPane(),JTextField.class).size()==4,"three session labels plus punishment");
                    check(app.getTitle().equals("Boom Boom Kiss"),"window title preserved");
                    for(String removed:new String[] {"Boom Boom Kiss","Theme của bạn","Bấm biểu tượng để chọn",
                            "2 boom + 1 kiss trên mỗi bàn","Mở trúng kiss trực tiếp hoặc do nổ lan là thua."})
                        check(labels(app,removed).isEmpty(),"removed old home text: "+removed);
                    for(String expected:new String[] {"Chọn biểu tượng theo ý bạn","Bấm vào ô để đổi biểu tượng","Luật chơi"})
                        check(!labels(app,expected).isEmpty(),"new home text: "+expected);
                    check(button(app,"CHƠI").getIcon()!=null,"play arrow retained");
                    for(JTextField field:find(app.getContentPane(),JTextField.class))
                        check(field.getHeight()>=22 && field.getWidth()>40,"fields fit window");
                    snapshot(app,previews.resolve(size.width==480?"11-home.png":"12-home-small.png"));
                }
                JTextField boom=field(app,"Nhãn BOOM"); boom.setText("Kẹo nổ");
                check(hasText(app,"Mỗi bàn giấu 2 Kẹo nổ và 1 Kiss"),"rules use editable session label");
                AbstractButton icon=null;
                for(AbstractButton candidate:find(app.getContentPane(),AbstractButton.class))
                    if("Đổi icon Kẹo nổ".equals(candidate.getAccessibleContext().getAccessibleName()))icon=candidate;
                check(icon!=null,"icon action remains linked to label");
                icon.doClick();
                JDialog palette=null;
                for(Window owned:app.getOwnedWindows())if(owned instanceof JDialog && owned.isVisible())palette=(JDialog)owned;
                check(palette!=null,"palette opens from actual icon click");
                List<AbstractButton> options=find(palette.getContentPane(),AbstractButton.class);
                options.get(1).doClick();
                check(theme.iconFor(CellType.BOOM).equals(IconCatalog.choices(CellType.BOOM)[1]),"real icon selection applied");
                check(writes[0]==1,"icon change saves theme once");
                boom.setText(""); boom.postActionEvent();
                field(app,"Punishment text").setText("   ");
                button(app,"CHƠI").doClick();
                check(theme.getLoseText().equals(Theme.DEFAULT_LOSE_TEXT),"blank uses single Vietnamese default");
                check(game.getState()==GameState.SETUP_PLAYER_1 && writes[0]==2,"play creates game and saves");
                Method menu=Main.class.getDeclaredMethod("showMenu"); menu.setAccessible(true); menu.invoke(app);
                check(field(app,"Nhãn BOOM").getText().equals("Boom"),"blank label defaults");
                field(app,"Nhãn BOOM").setText("Kẹo"); menu.invoke(app);
                check(field(app,"Nhãn BOOM").getText().equals("Kẹo"),"label survives navigation in session");
                Main fresh=new Main(game);
                try { check(field(fresh,"Nhãn BOOM").getText().equals("Boom"),"new window resets session labels"); }
                finally {fresh.dispose();}
                System.out.println("Home tests passed: no scroll, text, icon save, session labels and Vietnamese default");
            } catch(Exception error) { throw new RuntimeException(error); }
            finally {if(app!=null)app.dispose();}
        });
    }

    private static List<JLabel> labels(Main app,String text) {
        List<JLabel> out=new ArrayList<JLabel>();
        for(JLabel label:find(app.getContentPane(),JLabel.class))if(text.equals(label.getText()))out.add(label);
        return out;
    }
    private static boolean hasText(Main app,String text) {
        for(JLabel label:find(app.getContentPane(),JLabel.class))if(label.getText()!=null && label.getText().contains(text))return true;
        return false;
    }
    private static JTextField field(Main app,String name) {
        for(JTextField field:find(app.getContentPane(),JTextField.class))
            if(name.equals(field.getAccessibleContext().getAccessibleName()))return field;
        throw new AssertionError("Missing field "+name);
    }
    private static AbstractButton button(Main app,String name) {
        for(AbstractButton button:find(app.getContentPane(),AbstractButton.class))if(name.equals(button.getText()))return button;
        throw new AssertionError("Missing button "+name);
    }
    static <T> List<T> find(Container parent,Class<T> type) {
        List<T> out=new ArrayList<T>();
        for(Component child:parent.getComponents()) {
            if(type.isInstance(child))out.add(type.cast(child));
            if(child instanceof Container)out.addAll(find((Container)child,type));
        }
        return out;
    }
    private static void layout(Container parent) {
        parent.doLayout(); for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);
    }
    private static void snapshot(Main app,Path output) throws Exception {
        BufferedImage image=new BufferedImage(app.getRootPane().getWidth(),app.getRootPane().getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics(); app.getRootPane().printAll(g); g.dispose(); ImageIO.write(image,"png",output.toFile());
    }
    private static void check(boolean valid,String message) {if(!valid)throw new AssertionError(message);}
}
