import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public class BoggleBoard {
    private static JTextArea[][] arr = new JTextArea[4][4];
    static String[][] strArr = new String[4][4];
    private String[] dictionary;
    private BoggleBoard board;
    public java.util.List<String> results;


    public int rows() { return 4; }

    public int cols() { return 4; }

    public char getLetter(int i, int j) {
        return strArr[i][j].charAt(0);
    }

    public void initialize() {
        JFrame f = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        f.setSize(width / 2, height / 2);
        f.setTitle("Word Find Solver");

        JPanel panel = new JPanel(new GridLayout(4, 4, 5,5));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                arr[i][j]  = new JTextArea(4,4);
                int finalI = i;
                int finalJ = j;
                arr[i][j].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_TAB) {
                            if (e.getModifiers() > 0) {
                                arr[finalI][finalJ].transferFocusBackward();
                            } else {
                                arr[finalI][finalJ].transferFocus();
                            }
                            e.consume();
                        }
                    }
                });
                AbstractDocument pDoc=(AbstractDocument)arr[i][j].getDocument();
                pDoc.setDocumentFilter(new DocumentSizeFilter(1, BoggleBoard::tabNext));
                panel.add(arr[i][j]);
            }
        }

        JButton button = new JButton("Find Solutions");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        strArr[i][j] = arr[i][j].getText();
                    }
                }

                BoggleSolver solver = new BoggleSolver(dictionary);
                results = new ArrayList<>();
                for (String word : solver.getAllValidWords(board))
                {
                    results.add(word);
                }

                Collections.sort(results, (o1, o2) -> -Integer.valueOf(o1.length()).compareTo(o2.length()));
                StringBuilder sb = new StringBuilder();
                for (int i=0; i < 20; i++) {
                    int fromIndex = i*10;
                    if (results.size() <= fromIndex)
                        break;
                    sb.append(results.subList(fromIndex, fromIndex + Math.min(10, results.size() - fromIndex)));
                    sb.append("\n");
                }
                JOptionPane.showMessageDialog(null, sb, "InfoBox: ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel2.add(panel);
        panel2.add(button);
        f.add(panel2);

        f.setVisible(true);
        f.setLocationRelativeTo(null);
        Container c = f.getContentPane();
        c.setBackground(Color.DARK_GRAY);
        f.setResizable(false);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new FlowLayout());
    }

    public BoggleBoard(String[] dictionary) {
        this.board = this;
        this.dictionary = dictionary;
        initialize();
    }

    private static String tabNext(String string) {
        return string;
    }

    private class DocumentSizeFilter extends DocumentFilter {
        int maxCharacters;
        boolean DEBUG = false;
        Function<String,String> function;

        public DocumentSizeFilter(int maxChars, Function<String,String> function) {
            maxCharacters = maxChars;
            this.function = function;
        }

        public void insertString(FilterBypass fb, int offs,
                                 String str, AttributeSet a)
                throws BadLocationException {
            if (DEBUG) {
                System.out.println("in DocumentSizeFilter's insertString method");
            }

            //This rejects the entire insertion if it would make
            //the contents too long. Another option would be
            //to truncate the inserted string so the contents
            //would be exactly maxCharacters in length.
            if ((fb.getDocument().getLength() + str.length()) <= maxCharacters)
                super.insertString(fb, offs, str, a);
            else {
                function.apply("bogus");
            }
        }

        public void replace(FilterBypass fb, int offs,
                            int length,
                            String str, AttributeSet a)
                throws BadLocationException {
            if (DEBUG) {
                System.out.println("in DocumentSizeFilter's replace method");
            }
            //This rejects the entire replacement if it would make
            //the contents too long. Another option would be
            //to truncate the replacement string so the contents
            //would be exactly maxCharacters in length.
            if ((fb.getDocument().getLength() + str.length()
                    - length) <= maxCharacters)
                super.replace(fb, offs, length, str, a);
            else {
                Robot robot = null;
                try {
                    robot = new Robot();
                } catch (AWTException e) {
                    //
                }
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyPress(Integer.valueOf(KeyEvent.getExtendedKeyCodeForChar(str.charAt(0))));
            }
        }

    }

}
