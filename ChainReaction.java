import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class ChainReaction extends JFrame implements MouseListener, Runnable{
	Socket socket;
	PrintStream ps;
	Scanner read;
	int turn, noOfPlayers, id, winnerId;
	Color player[] = new Color[8];
	JPanel grid;
	Font font = new Font("temp", Font.BOLD, 20);
	JLabel yourColor, values[][]= new JLabel[8][6], playerId;
	JPanel blocks[][] = new JPanel[8][6];
	boolean gameOver, winner, hasPlayedOnce, allHavePlayedOnce;
	public ChainReaction(Socket socket, int noOfPlayers, int id) throws IOException {
		// TODO Auto-generated constructor stub
		this.socket = socket;
		player[0] = new Color(255, 0, 0);
		player[1] = new Color(0, 255, 0);
		player[2] = new Color(0, 0, 255);
		player[3] = new Color(0, 255, 255);
		player[4] = new Color(255, 0, 255);
		player[5] = new Color(255, 255, 0);
		player[6] = new Color(141, 27, 27);
		player[7] = new Color(19, 27, 62);
		this.noOfPlayers = noOfPlayers;
		this.id = id;
		hasPlayedOnce = false;
		allHavePlayedOnce = false;
		ps = new PrintStream(socket.getOutputStream());
		read = new Scanner(socket.getInputStream());
		System.out.println("No Of Players:" + noOfPlayers);
		System.out.println("Id:" + id);
		draw();
		new Thread(this).start();
	}
	void draw()
	{
		int i, j;
		grid = new JPanel();
		grid.setLayout(new GridLayout(8, 6));
		setLayout(null);
		for(i=0; i<8; i++)
			for(j=0; j<6; j++)
			{
				values[i][j] = new JLabel("");
				blocks[i][j] = new JPanel();
				values[i][j].setFont(font);
				blocks[i][j].add(values[i][j]);
				blocks[i][j].setSize(50, 50);
				blocks[i][j].setBackground(Color.BLACK);
				if(id == 0)
				blocks[i][j].addMouseListener(this);
				
				blocks[i][j].setBorder(BorderFactory.createLineBorder(player[0]));
				grid.add(blocks[i][j]);
			}
		yourColor = new JLabel("This Is Your Color");
		playerId = new JLabel("Player " + (id+1));
		playerId.setBounds(115, 0, 200, 30);
		add(playerId);
		yourColor.setForeground(player[id]);
		add(yourColor);
		yourColor.setBounds(75, 10, 200, 50);
		add(grid);
		grid.setBounds(0, 50, 300, 400);
		setVisible(true);
		setResizable(false);
		setSize(300, 500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		int temp, i, j;
		for(i=0; i<8; i++)
		for(j=0; j<6; j++)
			if(blocks[i][j] == arg0.getSource()){
				if(values[i][j].getText().equals(""))
				{
					values[i][j].setText("0");
					values[i][j].setForeground(player[turn]);
				}
				if(values[i][j].getForeground() == player[turn])
				{
					hasPlayedOnce = true;
					ps.println(i+" "+j);
				}
			}
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	boolean chkGameOver()
	{
		int i, j, nonZero=0, players=0;
		for(i=0; i<8; i++)
		for(j=0; j<6; j++)
		{
			if(!values[i][j].getText().equals(""))
			{
				nonZero++;
				if(values[i][j].getForeground() == player[id])
					players++;
			}
		}
		winner = false;
		if(players == 0)
			return true;
		if(players == nonZero)
			winner = true;
		return false;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int i, j, nextTurn;
		while(true)
		{
			String str, temp[];
			if(read.hasNext())
			{
				str = read.nextLine();
				System.out.println(str);
				temp = str.split(" ");
				i=Integer.parseInt(temp[0]);
				j=Integer.parseInt(temp[1]);
				dfs(i, j);
				if(allHavePlayedOnce)
				{
					if(id == turn){
						chkGameOver();
						if(winner)
							winnerId = id;
						else
							winnerId = -1;
						ps.println(winnerId);
					}
					str = read.nextLine();
					winnerId = Integer.parseInt(str);
					if(winnerId != -1)
					{
						JOptionPane.showMessageDialog(this,"Player " + (winnerId+1) + " wins");
						this.dispose();
						ps = null;
						System.exit(0);
					}
				}
				else if(turn == noOfPlayers-1)
					allHavePlayedOnce=true;
				if(hasPlayedOnce){
					if(chkGameOver()){
						ps.println("Game Over");
						yourColor.setText("Game Over :(");
					}
					else
						ps.println("Game Not Over");
				}
				else
					ps.println("Game Not Over");
				if(id == turn)
					ps.println("Calculate Turn");
				str = read.nextLine();
				turn = Integer.parseInt(str);
				for(i=0; i<8; i++)
				for(j=0; j<6; j++)
				{
					blocks[i][j].setBorder(BorderFactory.createLineBorder(player[turn]));
					if(id != turn)
						blocks[i][j].removeMouseListener(this);
					else
						blocks[i][j].addMouseListener(this);
				}
			}
		}
	}
	void dfs(int i, int j)
	{
		int temp;
		if(i < 0 || j<0 || i>7 || j>5)
			return ;
		values[i][j].setForeground(player[turn]);
		if(values[i][j].getText().equals(""))
			values[i][j].setText("0");
		temp = Integer.parseInt(values[i][j].getText());
		temp++;
		values[i][j].setText(""+temp);
		if(chkExplode(i, j)){
			values[i][j].setText("");
			dfs(i-1, j);
			dfs(i+1, j);
			dfs(i, j+1);
			dfs(i, j-1);
		}
	}
	boolean chkExplode(int i, int j)
	{
		int deg=0, val = Integer.parseInt(values[i][j].getText());
		if(i == 0 || i == 7)
			deg++;
		if(j == 0 || j == 5)
			deg++;
		if(4-deg == val)
			return true;
		return false;
	}
}
