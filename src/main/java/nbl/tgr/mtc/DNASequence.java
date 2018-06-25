package nbl.tgr.mtc;

public class DNASequence {
    protected final String seq_1, seq_2;    // The two sequences to be analyzed
    public int alignmentScore;              // Using Needleman-Wunsch algorithm
    protected Node[][] matrix;              // Store scores and indels

    protected final int matchScore, mismatchScore, indel;

    // Strings to be used to print the DNA sequnce analysis
    String top = "";        // Sequence 1
    String buffer = "";     // Matches between Sequnce 1 & 2
    String bottom = "";     // Sequence 2

    public DNASequence(String s1, String s2, ScoreScheme s) {
        // I use a ■ as a buffer so that the Sequence string aligns properly
        // with the indels and scores within the matrix.
        seq_1 = "\u25A0" + s1;
        seq_2 = "\u25A0" + s2;

        // Setup the scoring schema. For this program, I am implementing a simple
        // gap cost instead of complicating it further with gap extention and
        // opening costs.
        matchScore = s.matchScore;
        mismatchScore = s.mismatchScore;
        indel = s.indel;

        // I instiate the matrix and progressively build the indels on the first
        // row and the first column.
        matrix = new Node[seq_1.length()][seq_2.length()];
        for (int i = 0; i < seq_1.length(); i++)
            matrix[i][0] = new Node(i * indel, i, 0);

        for (int i = 0; i < seq_2.length(); i++)
            matrix[0][i] = new Node(i * indel, 0, i);
    }

    // Helper method that helps decide what kind of match/mismatch score to use.
    protected int score(int i, int j) {
        if (seq_1.charAt(i) == seq_2.charAt(j))
            return matchScore;
        else
            return mismatchScore;
    }

    // Helper method that implements the Needleman-Wunsch algo on a local level.
    protected Node match(int i, int j) {
        Node s1,s2,s3;
        s1 = new Node(matrix[i-1][j-1].score + score(i, j), i, j);
        s2 = new Node(matrix[i-1][j].score + indel, i, j);
        s3 = new Node(matrix[i][j-1].score + indel, i, j);

        // Since the aim of the Needleman-Wunsch algo is to find the shortest path
        // with the lowest cost and highest score, I check whichever of the viable
        // nodes nets me the highest score (up till that point). Once found, I set
        // a pointer back to the previous node (to make it easier to traceback)
        // and return the current node (to be placed in the matrix).
        Node largest = new Node(Math.max(s1.score, Math.max(s2.score, s3.score)), i, j);
        if (s1.compareTo(largest) == 0)
            largest.prev = matrix[i-1][j-1];
        else if(s2.compareTo(largest) == 0)
            largest.prev = matrix[i-1][j];
        else
            largest.prev = matrix[i][j-1];

        return largest;
    }

    // Runs the Needleman-Wunsch algo on every node in the matrix, sets the aligment
    // score and returns the last node in the matrix -- the one that holds the score.
    public Node runAnalysis() {
        for (int i = 1; i < seq_1.length(); i++) {
            for (int j = 1; j < seq_2.length(); j++){
                matrix[i][j] = match(i, j);
            }
        }
        alignmentScore = matrix[seq_1.length()-1][seq_2.length()-1].score;
        return matrix[seq_1.length()-1][seq_2.length()-1];
    }

    // Helper method that progressively builds the analysis result. It returns the 
    // 'tail' because we may still need to do some work on it.
    protected Node traceHelper(Node curr) {
        while (curr.prev != null) {
            // Print out the 'path'
            // System.out.print(curr.score + "[" + curr.i + ", " + curr.j + "] -> ");

            if (curr.i - curr.prev.i == 1 && curr.j - curr.prev.j == 1){    // If the path leads diagonal
                boolean x = seq_1.charAt(curr.i) == seq_2.charAt(curr.j) ? true : false;
                // System.out.println("Going diag: " + x);
                if(x){
                    top = seq_1.charAt(curr.i) + " " + top;
                    buffer = "|" + " " + buffer;
                    bottom = seq_2.charAt(curr.j) + " " + bottom;
                }else{
                    top = seq_1.charAt(curr.i) + " " + top;
                    buffer = " " + " " + buffer;
                    bottom = seq_2.charAt(curr.j) + " " + bottom;
                }
            }else if (curr.i - curr.prev.i == 1){                           // If the path leads up
                // System.out.println("Going up: " + indel);
                top = seq_1.charAt(curr.i) + " " + top;
                buffer = " " + " " + buffer;
                bottom = "-" + " " + bottom;                                // If the path leads left
            }else if (curr.j - curr.prev.j == 1){
                // System.out.println("Going left: " + indel);
                top = "-" + " " + top;
                buffer = " " + " " + buffer;
                bottom = seq_2.charAt(curr.j) + " " + bottom;
            }

            curr = curr.prev;
        }

        return curr;
    }

    // Traceback from the last node in the matrix back to the first indel node.
    public void traceback() {
        Node curr = matrix[seq_1.length()-1][seq_2.length()-1];

        curr = traceHelper(curr);

        // Sometimes the tail or the traceback path ends on an indel. As a result,
        // we end up with an incomplete path because the algorithm doesn't see past
        // it. To counter that, I check if I am on an indel and if so, I help it
        // move along the path back to 0,0.
        while (curr.i != 0 || curr.j != 0) {
            if (curr.i != 0 && curr.j == 0){
                curr.prev = matrix[curr.i-1][curr.j];
                curr = traceHelper(curr);
            }else if (curr.i == 0 && curr.j != 0) {
                curr.prev = matrix[curr.i][curr.j-1];
                curr = traceHelper(curr);
            }
        }

        // Print out the DNA sequence analysis
        System.out.println(top);
        System.out.println(buffer);
        System.out.println(bottom);
    }

    // Print the matrix.
    public void printMatrix() {
        System.out.printf("%4s", "\u25A0");
        for (int i = 0; i < matrix[0].length; i++) {
            System.out.printf("%4s", seq_2.charAt(i));
        }
        System.out.println();
        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("%4s", seq_1.charAt(i));
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%4d", matrix[i][j].score);
            }
            System.out.println();
        }
    }

    // Creates a random DNA sequence -- for testing purposes.
    public static String randseq(int n) {
        char[] S = new char[n];
        String DNA = "ACGT";

        for (int i = 0; i < n; i++) {
            int r = (int)(Math.random() * 4);
            S[i] = DNA.charAt(r);
        }

        return new String(S);
    }

    public static void main(String[] args) {
        // Test with randomly generated DNA sequences
        //String seq_1 = randseq(34);
        //String seq_2 = randseq(32);

        // Or test with the following
         String seq_1 = "CATTAATTACACTCTCGCACTCACCACCAAACATCCTAAACCCAGACAGGCCTCGACTCC";
         String seq_2 = "ACTAAACAAGACTCGCCTGTCTAACTAGGGAGTTTATAATGAACCGTGGCGTAGACCA";

        ScoreScheme s = new ScoreScheme(2, -1, -2);             // MatchScore: 2; MismatchScore: -2; Indel: -2
        DNASequence dna = new DNASequence(seq_1, seq_2, s);     // Create a new DNA Sequence using two strands

        dna.runAnalysis();

        dna.traceback();
        System.out.println("The alignment score is " + dna.alignmentScore);

         dna.printMatrix();
    }
}

class Node implements Comparable<Node>{
    int i, j;
    int score;
    Node prev;

    public Node(int score, int x, int y) {
        this.i = x;
        this.j = y;
        this.score = score;
        this.prev = null;
    }

    public int compareTo(Node n) {
        return this.score - n.score;
    }

    public String toString() {
        return ""+score;
    }
}

class ScoreScheme {
    int matchScore, mismatchScore, indel;

    public ScoreScheme(int m1, int m2, int i) {
        matchScore = m1;
        mismatchScore = m2;
        indel = i;
    }
}