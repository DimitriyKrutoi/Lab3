package com.company;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main extends JFrame {
    private static final int WIDTH = 700; //размер окна
    private static final int HEIGHT = 500;

    private JFileChooser fileChooser = null; // Объект диалогового окна для выбора файлов


    private JMenuItem saveToTextMenuItem;//Элементы меню
    private JMenuItem searchValueMenuItem;
    private JMenuItem infoMenuItem;

    private JCheckBoxMenuItem showColumnMenuItem;

    private JTextField textFieldFrom;
    private JTextField textFieldTo;      // Поля ввода для считывания значений переменных
    private JTextField textFieldStep;
    private Box hBoxResult;

    private FunctionTableCellRenderer renderer = new FunctionTableCellRenderer();// Визуализатор ячеек таблицы
    private FunctionTableModel data;// Модель данных с результатами вычислений

    private JTable table;
    private TableColumn bool_column;
    private Double param = -1.0;

    public Main(){
        super("Табулирование функции на отрезке");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        // Отцентрировать окно приложения на экране
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);

        JMenuBar menuBar = new JMenuBar();//Создание меню
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");// Добавить в меню пункт меню "Файл"
        menuBar.add(fileMenu);
        JMenu tableMenu = new JMenu("Таблица");
        menuBar.add(tableMenu);
        JMenu infoMenu = new JMenu("Справка");
        menuBar.add(infoMenu);

        Action saveToTextAction = new AbstractAction( "Сохранить в текстовый файл") {//сохраняем в текст. файл
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {// Если экземпляр диалогового окна "Открыть файл" еще не создан,то создать его
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));//инициализировать текущей директорией
                }
                // Показать диалоговое окно
                if (fileChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION){
                    saveToTextFile(fileChooser.getSelectedFile());//сохраняем в текст файл
                }
            }
        };
        saveToTextMenuItem = fileMenu.add(saveToTextAction);// Добавить соответствующий пункт подменю в меню "Файл"

        fileMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (data == null) saveToTextMenuItem.setEnabled(false);// По умолчанию пункт меню является недоступным (данных ещѐ нет)
                else saveToTextMenuItem.setEnabled(true);
            }
            @Override
            public void menuDeselected(MenuEvent e) { }
            @Override
            public void menuCanceled(MenuEvent e) { }
        });

        Action searchValueAction = new AbstractAction("Найти значение функции") {// Создать новое действие по поиску значений многочлена
            public void actionPerformed(ActionEvent event) {
                // Запросить пользователя ввести искомую строку
                String value = JOptionPane.showInputDialog(Main.this, "Введите значение для поиска", "Поиск значения", JOptionPane.QUESTION_MESSAGE);
                // Установить введенное значение в качестве иголки
                renderer.setNeedle(value);
                getContentPane().repaint();//обновляем таблицу
            }
        };

        searchValueMenuItem = tableMenu.add(searchValueAction);// Добавить действие в меню "Таблица"
        tableMenu.add(new JSeparator());
        showColumnMenuItem = new JCheckBoxMenuItem("Показать третий столбец", true);
        tableMenu.add(showColumnMenuItem);    // третий столбец
        showColumnMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 2) {
                    bool_column = table.getColumnModel().getColumn(2);
                    table.removeColumn(bool_column);    // не будет 3 столбца будет 2
                }if(e.getStateChange() == 1){
                    table.addColumn(bool_column);
                }
            }
        });

        Action aboutProgrammAction = new AbstractAction("О программе") {
            public void actionPerformed(ActionEvent event) {
                JLabel info = new JLabel("8 группа 2 курс Байдун Дмитрий");
                info.setHorizontalTextPosition(JLabel.CENTER);
                info.setVerticalTextPosition(JLabel.BOTTOM);    // о программе
                info.setIconTextGap(10);
                JOptionPane.showMessageDialog(Main.this, info, "О программе", JOptionPane.PLAIN_MESSAGE);
            }
        };
        infoMenuItem = infoMenu.add(aboutProgrammAction);

        textFieldFrom = new JTextField("0.0", 10);
        textFieldFrom.setMaximumSize(textFieldFrom.getPreferredSize());
        textFieldTo = new JTextField("1.0", 10); //область с полями
        textFieldTo.setMaximumSize(textFieldTo.getPreferredSize());
        textFieldStep = new JTextField("0.1", 10);
        textFieldStep.setMaximumSize(textFieldStep.getPreferredSize());

        Box hboxXRange = Box.createHorizontalBox();//реализовываем коробку
        hboxXRange.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Настройки:"));
        hboxXRange.add(Box.createHorizontalGlue());
        hboxXRange.add(new JLabel("X изменяется на интервале от:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldFrom);
        hboxXRange.add(Box.createHorizontalStrut(20));
        hboxXRange.add(new JLabel("до:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldTo);
        hboxXRange.add(Box.createHorizontalStrut(20));
        hboxXRange.add(new JLabel("с шагом:"));
        hboxXRange.add(Box.createHorizontalStrut(10));
        hboxXRange.add(textFieldStep);
        hboxXRange.add(Box.createHorizontalStrut(20));
        hboxXRange.add(Box.createHorizontalGlue());
        // Установить предпочтительный размер области равным удвоенному
        // минимальному, чтобы при компоновке область совсем не сдавили
        hboxXRange.setPreferredSize(new Dimension((int)(hboxXRange.getMaximumSize().getWidth()), (int)(hboxXRange.getMinimumSize().getHeight()*1.5)));
        getContentPane().add(hboxXRange, BorderLayout.NORTH);// Установить область в верхнюю (северную) часть компоновки

        JButton buttonCalc = new JButton("Вычислить"); //кнопка
        buttonCalc.addActionListener(new ActionListener() { // Задать действие на нажатие "Вычислить" и привязать к кнопке
            public void actionPerformed(ActionEvent ev) {
                try {
                    showColumnMenuItem.setState(true);
                    // Считать значения начала и конца отрезка, шага
                    Double from = Double.parseDouble(textFieldFrom.getText());
                    Double to = Double.parseDouble(textFieldTo.getText());
                    Double step = Double.parseDouble(textFieldStep.getText());
                    // На основе считанных данных создать новый экземпляр модели таблицы
                    data = new FunctionTableModel(from, to, step, param);

                    table = new JTable(data);//новый экземпляр табл
                    // Установить в качестве визуализатора ячеек для класса Double разработанный визуализатор
                    table.setDefaultRenderer(Double.class, renderer);
                    table.setRowHeight(30);// Установить размер строки таблицы в 30 пикселов

                    // Удалить все вложенные элементы из контейнера hBoxResult
                    hBoxResult.removeAll();
                    // Добавить в hBoxResult таблицу, "обернутую" в панель с полосами прокрутки
                    hBoxResult.add(new JScrollPane(table));
                    hBoxResult.revalidate();
                } catch (NumberFormatException ex) {// В случае ошибки преобразования чисел показать сообщение об ошибке
                    JOptionPane.showMessageDialog(Main.this, "Ошибка в формате записи числа с плавающей точкой", "Ошибочный формат числа", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JButton buttonReset = new JButton("Очистить поля");//кнопка
        // Задать действие на нажатие "Очистить поля" и привязать к кнопке
        buttonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // Установить в полях ввода значения по умолчанию
                textFieldFrom.setText("0.0");
                textFieldTo.setText("1.0");
                textFieldStep.setText("0.1");
                // Удалить все вложенные элементы контейнера hBoxResult
                hBoxResult.removeAll();// Удалить все вложенные элементы из контейнера hBoxResult
                hBoxResult.repaint();
                data = null;
            }
        });

        // Поместить созданные кнопки в контейнер
        Box hboxButtons = Box.createHorizontalBox();
        hboxButtons.setBorder(BorderFactory.createEtchedBorder());
        hboxButtons.add(Box.createHorizontalGlue());
        hboxButtons.add(buttonCalc);
        hboxButtons.add(Box.createHorizontalStrut(30));
        hboxButtons.add(buttonReset);
        hboxButtons.add(Box.createHorizontalGlue());

        // Установить предпочтительный размер области равным удвоенному минимальному, чтобы при компоновке окна область совсем не сдавили

        hboxButtons.setPreferredSize(new Dimension((int)(hboxButtons.getMaximumSize().getWidth()), (int)(hboxButtons.getMinimumSize().getHeight() * 2)));

        // Разместить контейнер с кнопками в нижней (южной) области граничной компоновки
        getContentPane().add(hboxButtons, BorderLayout.SOUTH);
        hBoxResult = Box.createHorizontalBox();
        // Установить контейнер hBoxResult в главной (центральной) области граничной компоновки
        getContentPane().add(hBoxResult, BorderLayout.CENTER);
    }

    protected void saveToTextFile(File selectedFile) {
        try{
            // Создать новый байтовый поток вывода, направленный в указанный файл
            PrintStream out = new PrintStream(selectedFile);
            // Записать в поток вывода заголовочные сведения
            out.println("Результаты табулирования функции:");
            out.println("Интервал от " + data.getFrom() + " до " + data.getTo()+ " с шагом " + data.getStep() + " и параметром " + data.getParameter());
            for (int i = 0; i < data.getRowCount(); i++)
            {
                out.println("Значение в точке " + data.getValueAt(i,0)  + " равно " + data.getValueAt(i,1));
            }
            //закрыть поток
            out.close();
        } catch (FileNotFoundException e){//файл не найден
        }
    }

    public static void main(String[] args){
        Main frame = new Main();
        // Задать действие, выполняемое при закрытии окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}