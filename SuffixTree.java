package myukkonen;

import java.util.Arrays;




/**
 * @author ivy
 *
 * 2018.8.1  
 * 
 * https://bl
 * og.csdn.net/vickyway/article/details/50059095
 */


/**
 * 规则1（向根节点分割并插入一条边）：
 *   活动节点保留为根节点
 *   设置活动边为我们要插入的新后缀的第一个字符
 *   活动长度减一
 * 规则2：
 *   如果我们分割一条边并插入新节点，而且如果他不是在当前步骤里创建的第一个节点，我们通过后缀链接（把以前插入的节点和新增节点链接起来）
 * 规则3：
 *   分割从不是根节点的活动节点开始的边之后，紧跟着从活动点开始的后缀链接
 */


public class SuffixTree {
	
	//节点
	private class Node {
		public char[] chars;
		public Node child;
		public Node brother;
		public Node suffixNode;
		
		public Node(char[] chars) {
			this.chars=chars;
		}
		
		public String toString() {
			return "Node [chars="+String.valueOf(chars)+"]";
		}
		
		public void print(String prefix) {
			System.out.print(String.valueOf(chars));
			if(this.suffixNode!=null) {
				System.out.println("--"+String.valueOf(this.suffixNode.chars));
			}else {
				System.out.println();
			}
			Node child=this.child;
			while(child!=null) {
				System.out.print(prefix+"|——");
				child.print(prefix + prefix);
				child=child.brother;
			}
		}
	}
	
	//活动节点
	private class ActivePoint{
		public Node point;
		public Node index;
		public int length;
		
		public ActivePoint(Node point,Node index,int length) {
			this.point=point;
			this.index=index;
			this.length=length;
		}
		
		public String toString() {
			return "ActivePoint [point=" + point + ", index=" + index + ", length=" + length + "]";
		}
	}
	
	private Node root=new Node(new char[0]);//根节点
	private int reminder=0;//剩余后缀树
	private ActivePoint activepoint= new ActivePoint(root,null,0);//
	
	/**
	 * 寻找当前活动节点中是否包含后缀字符的节点
	 * @param w
	 * @return
	 *
	 */
	private boolean find(char w) {
		final Node start = activepoint.point;
		final Node current = activepoint.index;
		boolean exist = false;
		if (null == current) {// current==null 无活动边，则从活动点的子节点开始查找
			// 寻找子节点
			Node child = start.child;
			while (null != child) {
				if (child.chars[0] == w) {// 存在
					activepoint.index = child;
					activepoint.length++;// activePoint.length++
					exist = true;
					break;
				} else {
					child = child.brother;
				}
			}
		} else if (current.chars[activepoint.length] == w) {// 有活动边，则在活动边上查找
			activepoint.length++;
			exist = true;
			if (current.chars.length == activepoint.length) {
				// 如果活动边的长度已达到活动边的最后一个字符，则将活动点置为活动边，同时活动边置为null，长度置为0
				activepoint.point = current;
				activepoint.index = null;
				activepoint.length = 0;
			}
		} else {
			exist = false;
		}
		return exist;
	}
	
	/**
	 * 处理插入后缀
	 * 
	 * @param chars 构建后追树的全部字符
	 * @param currentIndex 当前处理到的字符位置
	 * @param prefixNode 前继节点，已经进行分割的节点，用于标识后缀节点
	 */
	private void innerSplit(char[] chars, int currenctIndex, Node prefixNode) {
		// 此处计算剩余待插入的后缀的开始位置，例如我们需要插入三个后缀(abx,bx,x)，已处理了abx，则还剩余ba和x，则下面计算的位置就是b的位置
		int start = currenctIndex - reminder + 1;
		
		this.print();// 打印
		System.out.println();
		System.out.println("当前插入后缀：" + String.copyValueOf(chars, start, currenctIndex - start + 1) + "========");
		
		// dealStart表示本次插入我们需要进行查找的开始字符位置，因为由于规则2，可能出现通过后缀节点直接找到活动节点的情况
		// 如通过ab节点的后缀节点，直接找到节点b，那么此时的activePoint(node[b], null, 0)，我们需要从node[b]开始查找x，dealStart的位置就是x的位置
		int dealStart = start + activepoint.point.chars.length + activepoint.length;
		// 从dealStart开始查找所有后缀字符是否都存在(相对与活动点)
		for (int index = dealStart; index <= currenctIndex; index++) {
			char w = chars[index];
			if (find(w)) {// 存在，则查找下一个，activePoint.length+1，这里不增加reminder
				continue;
			}
			Node splitNode = null;// 被分割的节点
			if(activepoint.index==null){// 如果activePoint.index==null，说明没有找到活动边，那么只需要在活动节点下插入一个节点即可
				Node node = new Node(Arrays.copyOfRange(chars, index, chars.length));
				Node child = activepoint.point.child;
				if(null==child){
					activepoint.point.child = node;
				}else{
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
			}else{
				// 开始分割，分割部分同上面的分割
				splitNode = activepoint.index;
				// 创建切分后的节点，放到当前节点的子节点
				// 该节点继承了当前节点的子节点以及后缀节点信息
				Node node = new Node(Arrays.copyOfRange(splitNode.chars, activepoint.length, splitNode.chars.length));
				node.child = splitNode.child;
				node.suffixNode = splitNode.suffixNode;
				splitNode.child = node;
				splitNode.suffixNode = null;
				// 创建新插入的节点，放到当前节点的子节点(通过子节点的兄弟节点保存)
				Node newNode = new Node(Arrays.copyOfRange(chars, index, chars.length));
				splitNode.child.brother = newNode;
				// 修改当前节点的字符数
				splitNode.chars = Arrays.copyOfRange(splitNode.chars, 0, activepoint.length);
				// 规则2，连接后缀节点
				prefixNode.suffixNode = splitNode;
			}
			// --
			reminder--;

			// 按照规则1进行处理
			if (root == activepoint.point) {// 活动节点是根节点的情况
				// activePoint.point == root
			
			// 按照规则3进行处理
			} else if (null == activepoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
				activepoint.point = root;
			} else {
				activepoint.point = activepoint.point.suffixNode;
			}
			
			activepoint.index = null;
			activepoint.length = 0;
			if(reminder > 0){// 如果reminder==0则不需要继续递归插入后缀
				innerSplit(chars, currenctIndex, splitNode);
			}
		}
	}
	
	

	/**
	 * 格式化打印出整个后缀树
	 */
	public void print() {
		Node child = root.child;
		System.out.println("[root] [activePoint:(" + activepoint.point + "," + activepoint.index + ","+ activepoint.length + ")], [reminder:" + reminder + "]");
		while (child != null) {
			System.out.print("|——");
			child.print("    ");
			child = child.brother;
		}
	}

	
	/**
	 * 构建后缀树
	 * 
	 * @param word
	 *
	 */
	public void build(String word) {
		int index = 0;
		char[] chars = word.toCharArray();
		while (index < chars.length) {// 循环建立后缀
			int currenctIndex = index++;// 保存当前的位置
			char w = chars[currenctIndex];// 当前的后缀字符
			
			this.print();// 打印
			System.out.println();
			System.out.println("当前插入后缀：" + w + "========");

			if (find(w)) {// 查找是否存在保存有当前后缀字符的节点
				reminder++;// 存在，则将reminder+1，activePoint.length+1，然后返回即可
				continue;
			}

			// 不存在的话，如果reminder==0表示之前在该字符之前未剩余有其他带插入的后缀字符，所以直接插入该后缀字符即可
			if (reminder == 0) {
				// 直接在当前活动节点插入一个节点即可
				// 这里插入的节点包含的字符是从当前字符开始该字符串剩余的全部字符，这里是一个优化，
				// 优化参考自：http://blog.csdn.net/v_july_v/article/details/6897097 (3.6、归纳, 反思, 优化)
				Node node = new Node(Arrays.copyOfRange(chars, currenctIndex, chars.length));
				// 如果当前活动点无子节点，则将新建的节点作为其子节点即可，否则循环遍历子节点(通过兄弟节点进行保存)
				Node child = activepoint.point.child;
				if (null == child) {
					activepoint.point.child = node;
				} else {
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
			} else {
				// 如果reminder>0，则说明该字符之前存在剩余字符，需要进行分割，然后插入新的后缀字符
				Node splitNode = activepoint.index;// 待分割的节点即为活动边(active_edge)
				// 创建切分后的节点，放到当前节点的子节点
				// 该节点继承了当前节点的子节点以及后缀节点信息
				Node node = new Node(Arrays.copyOfRange(splitNode.chars, activepoint.length, splitNode.chars.length));// 从活动边长度开始截取剩余字符作为子节点
				node.child = splitNode.child;
				node.suffixNode = splitNode.suffixNode;
				splitNode.child = node;
				splitNode.suffixNode = null;
				// 创建新插入的节点，放到当前节点的子节点(通过子节点的兄弟节点保存)
				Node newNode = new Node(Arrays.copyOfRange(chars, currenctIndex, chars.length));// 插入新的后缀字符
				splitNode.child.brother = newNode;
				splitNode.chars = Arrays.copyOfRange(splitNode.chars, 0, activepoint.length);// 修改当前节点的字符

				// 分割完成之后需根据规则1和规则3进行区分对待
				// 按照规则1进行处理
				if (root == activepoint.point) {// 活动节点是根节点的情况
					// activePoint.point == root
				// 按照规则3进行处理
				} else if (null == activepoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
					activepoint.point = root;
				} else {// 否则活动节点变为当前活动节点的后缀节点
					activepoint.point = activepoint.point.suffixNode;
				}
				// 活动边和活动边长度都重置
				activepoint.index = null;
				activepoint.length = 0;
				// 递归处理剩余的待插入后缀
				innerSplit(chars, currenctIndex, splitNode);
			}
		}
	}
	
	//测试
	public static void main(String[] args) {
		SuffixTree tree = new SuffixTree();
		tree.build("abcabxabcd");
		tree.print();
	}
}
