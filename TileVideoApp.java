package apps;

import codec.*;
import io.*;
import models.SourceModel;
import models.Symbol;
import models.SymbolModel;
import models.Unsigned8BitModel;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;

public class TileVideoApp {
    public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
        String base = "pinwheel";
        String filename= "C:/Users/chipm/IdeaProjects/590Assn1/Huffman/Videos/" + base + ".450p.yuv";
        File file = new File(filename);
        int width = 800;
        int height = 450;
        int num_frames = 150;

//    [TL][TR] Each box is 400 x 150 pix
//    [ML][MR]
//    [BL][BR]

        Unsigned8BitModel TL = new Unsigned8BitModel();
        Unsigned8BitModel ML = new Unsigned8BitModel();
        Unsigned8BitModel BL = new Unsigned8BitModel();
        Unsigned8BitModel TR = new Unsigned8BitModel();
        Unsigned8BitModel MR = new Unsigned8BitModel();
        Unsigned8BitModel BR = new Unsigned8BitModel();

        InputStream training_values = new FileInputStream(file);
        int[][] emptycell = new int[width/2][height/3];
        ArrayList<int[][]> current_frame = new ArrayList<>();
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);

        for (int f=0; f < num_frames; f++) {
            System.out.println("Training frame difference " + f);
            ArrayList<int[][]> prior_frame = current_frame;
            current_frame = readFrame(training_values, width, height);

            int[][] diff_tile = frameDifference(prior_frame.get(0), current_frame.get(0));
            trainModelWithFrame(TL, diff_tile);
            diff_tile = frameDifference(prior_frame.get(1),current_frame.get(1));
            trainModelWithFrame(ML,diff_tile);
            diff_tile = frameDifference(prior_frame.get(2),current_frame.get(2));
            trainModelWithFrame(BL,diff_tile);
            diff_tile = frameDifference(prior_frame.get(3),current_frame.get(3));
            trainModelWithFrame(TR,diff_tile);
            diff_tile = frameDifference(prior_frame.get(4),current_frame.get(4));
            trainModelWithFrame(MR,diff_tile);
            diff_tile = frameDifference(prior_frame.get(5),current_frame.get(5));
            trainModelWithFrame(BR,diff_tile);
        }
        training_values.close();

        SymbolEncoder encoderTL = new HuffmanEncoder(TL,1000000000);
        SymbolEncoder encoderML = new HuffmanEncoder(ML,1000000000);
        SymbolEncoder encoderBL = new HuffmanEncoder(BL,1000000000);
        SymbolEncoder encoderTR = new HuffmanEncoder(TR,1000000000);
        SymbolEncoder encoderMR = new HuffmanEncoder(MR,1000000000);
        SymbolEncoder encoderBR = new HuffmanEncoder(BR,1000000000);

        Symbol[] symbolsTL = new Unsigned8BitModel.Unsigned8BitSymbol[256];
        Symbol[] symbolsML = new Unsigned8BitModel.Unsigned8BitSymbol[256];
        Symbol[] symbolsBL = new Unsigned8BitModel.Unsigned8BitSymbol[256];
        Symbol[] symbolsTR = new Unsigned8BitModel.Unsigned8BitSymbol[256];
        Symbol[] symbolsMR = new Unsigned8BitModel.Unsigned8BitSymbol[256];
        Symbol[] symbolsBR = new Unsigned8BitModel.Unsigned8BitSymbol[256];

        for (int v=0; v<256; v++) {
            SymbolModel s = TL.getByIndex(v);
            Symbol sym = s.getSymbol();
            symbolsTL[v] = sym;
            long prob = s.getProbability(TL.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + TL.getCountTotal());

            s = ML.getByIndex(v);
            sym = s.getSymbol();
            symbolsML[v] = sym;
            prob = s.getProbability(ML.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + ML.getCountTotal());

            s = BL.getByIndex(v);
            sym = s.getSymbol();
            symbolsBL[v] = sym;
            prob = s.getProbability(BL.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + BL.getCountTotal());

            s = TR.getByIndex(v);
            sym = s.getSymbol();
            symbolsTR[v] = sym;
            prob = s.getProbability(TR.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + TR.getCountTotal());

            s = MR.getByIndex(v);
            sym = s.getSymbol();
            symbolsMR[v] = sym;
            prob = s.getProbability(MR.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + MR.getCountTotal());

            s = BR.getByIndex(v);
            sym = s.getSymbol();
            symbolsBR[v] = sym;
            prob = s.getProbability(BR.getCountTotal());
            System.out.println("Symbol: " + sym + " probability: " + prob + "/" + BR.getCountTotal());
        }

        InputStream message = new FileInputStream(file);

        File out_file = new File("C:/Users/chipm/IdeaProjects/590Assn1/Huffman/Videos/" + base + "-compressed.dat");
        OutputStream out_stream = new FileOutputStream(out_file);
        BitSink bit_sink = new OutputStreamBitSink(out_stream);

        current_frame = new ArrayList<>();
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);
        current_frame.add(emptycell);

        for (int f=0; f < num_frames; f++) {
            System.out.println("Encoding frame difference " + f);
            ArrayList<int[][]> prior_frame = current_frame;
            current_frame = readFrame(message, width, height);

            int[][] diff_frame = frameDifference(prior_frame.get(0), current_frame.get(0));
            encodeFrameDifference(diff_frame, encoderTL, bit_sink, symbolsTL);
            diff_frame = frameDifference(prior_frame.get(1), current_frame.get(1));
            encodeFrameDifference(diff_frame, encoderML, bit_sink, symbolsML);
            diff_frame = frameDifference(prior_frame.get(2), current_frame.get(2));
            encodeFrameDifference(diff_frame, encoderBL, bit_sink, symbolsBL);
            diff_frame = frameDifference(prior_frame.get(3), current_frame.get(3));
            encodeFrameDifference(diff_frame, encoderTR, bit_sink, symbolsTR);
            diff_frame = frameDifference(prior_frame.get(4), current_frame.get(4));
            encodeFrameDifference(diff_frame, encoderMR, bit_sink, symbolsMR);
            diff_frame = frameDifference(prior_frame.get(5), current_frame.get(5));
            encodeFrameDifference(diff_frame, encoderMR, bit_sink, symbolsMR);
        }

        message.close();
        encoderTL.close(bit_sink);
        encoderML.close(bit_sink);
        encoderBL.close(bit_sink);
        encoderTR.close(bit_sink);
        encoderMR.close(bit_sink);
        encoderBR.close(bit_sink);
        out_stream.close();

        BitSource bit_source = new InputStreamBitSource(new FileInputStream(out_file));
        OutputStream decoded_file = new FileOutputStream(new File("C:/Users/chipm/IdeaProjects/590Assn1/Huffman/Videos/" + base + "-decoded.dat"));

        //		SymbolDecoder decoder = new HuffmanDecoder(encoder.getCodeMap());
//        SymbolDecoder decoderTL = new ArithmeticDecoder(TL);
//        SymbolDecoder decoderML = new ArithmeticDecoder(ML);
//        SymbolDecoder decoderBL = new ArithmeticDecoder(BL);
//        SymbolDecoder decoderTR = new ArithmeticDecoder(TR);
//        SymbolDecoder decoderMR = new ArithmeticDecoder(MR);
//        SymbolDecoder decoderBR = new ArithmeticDecoder(BR);
        Map<Symbol, String> codemap = HuffmanEncoder.createCodeMapFromModel(TL,1000000000);
        SymbolDecoder decoderTL = new HuffmanDecoder(codemap);
        codemap = HuffmanEncoder.createCodeMapFromModel(ML,1000000000);
        SymbolDecoder decoderML = new HuffmanDecoder(codemap);
        codemap = HuffmanEncoder.createCodeMapFromModel(BL,1000000000);
        SymbolDecoder decoderBL = new HuffmanDecoder(codemap);
        codemap = HuffmanEncoder.createCodeMapFromModel(TR,1000000000);
        SymbolDecoder decoderTR = new HuffmanDecoder(codemap);
        codemap = HuffmanEncoder.createCodeMapFromModel(MR,1000000000);
        SymbolDecoder decoderMR = new HuffmanDecoder(codemap);
        codemap = HuffmanEncoder.createCodeMapFromModel(BR,1000000000);
        SymbolDecoder decoderBR = new HuffmanDecoder(codemap);

        int[][] outFrame = new int[width][height];

        for (int f=0; f<num_frames; f++) {
            System.out.println("Decoding frame " + f);
            ArrayList<int[][]> prior_frame = breakIntoList(outFrame,width,height);
            int[][] diffTL = decodeFrame(decoderTL, bit_source, width/2, height/3);
            int[][] diffML = decodeFrame(decoderML, bit_source, width/2, height/3);
            int[][] diffBL = decodeFrame(decoderBL, bit_source, width/2, height/3);
            int[][] diffTR = decodeFrame(decoderTR, bit_source, width/2, height/3);
            int[][] diffMR = decodeFrame(decoderMR, bit_source, width/2, height/3);
            int[][] diffBR = decodeFrame(decoderBR, bit_source, width/2, height/3);
            ArrayList<int[][]> diff_frame = new ArrayList<>();
            diff_frame.add(diffTL);
            diff_frame.add(diffML);
            diff_frame.add(diffBL);
            diff_frame.add(diffTR);
            diff_frame.add(diffMR);
            diff_frame.add(diffBR);

            outFrame = reconstructFrame(prior_frame, diff_frame);
            outputFrame(outFrame, decoded_file);
        }

        decoded_file.close();

    }
    private static ArrayList<int[][]> readFrame(InputStream src, int width, int height)
            throws IOException {
        int[][] frame_data = new int[width][height];
        int[][] TL = new int[width/2][height/3];
        int[][] ML = new int[width/2][height/3];
        int[][] BL = new int[width/2][height/3];
        int[][] TR = new int[width/2][height/3];
        int[][] MR = new int[width/2][height/3];
        int[][] BR = new int[width/2][height/3];

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (x < width/2 && y < height/3) {
                    TL[x][y] = src.read();
                } else if (x < width/2 && y < 2*height/3) {
                    ML[x][y%150] = src.read();
                } else if (x < width/2) {
                    BL[x][y%150] = src.read();
                } else if (y < height/3) {
                    TR[x%400][y] = src.read();
                } else if (y < 2*height/3) {
                    MR[x%400][y%150] = src.read();
                } else {
                    BR[x%400][y%150] = src.read();
                }
            }
        }
        ArrayList<int[][]> tiles = new ArrayList<>();
        tiles.add(TL);
        tiles.add(ML);
        tiles.add(BL);
        tiles.add(TR);
        tiles.add(MR);
        tiles.add(BR);
        return tiles;
    }

    private static ArrayList<int[][]> breakIntoList(int[][] frame, int width, int height) {
        int[][] TL = new int[width/2][height/3];
        int[][] ML = new int[width/2][height/3];
        int[][] BL = new int[width/2][height/3];
        int[][] TR = new int[width/2][height/3];
        int[][] MR = new int[width/2][height/3];
        int[][] BR = new int[width/2][height/3];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (x < width/2 && y < height/3) {
                    TL[x][y] = frame[x][y];
                } else if (x < width/2 && y < 2*height/3) {
                    ML[x][y%150] = frame[x][y];
                } else if (x < width/2) {
                    BL[x][y%150] = frame[x][y];
                } else if (y < height/3) {
                    TR[x%400][y] = frame[x][y];
                } else if (y < 2*height/3) {
                    MR[x%400][y%150] = frame[x][y];
                } else {
                    BR[x%400][y%150] = frame[x][y];
                }
            }
        }
        ArrayList<int[][]> tiles = new ArrayList<>();
        tiles.add(TL);
        tiles.add(ML);
        tiles.add(BL);
        tiles.add(TR);
        tiles.add(MR);
        tiles.add(BR);
        return tiles;
    }

    private static int[][] frameDifference(int[][] prior_frame, int[][] current_frame) {
        int width = prior_frame.length;
        int height = prior_frame[0].length;

        int[][] difference_frame = new int[width][height];

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                difference_frame[x][y] = ((current_frame[x][y] - prior_frame[x][y])+256)%256;
            }
        }
        return difference_frame;
    }

    private static void trainModelWithFrame(Unsigned8BitModel model, int[][] frame) {
        int width = frame.length;
        int height = frame[0].length;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                model.train(frame[x][y]);
            }
        }
    }

    private static void encodeFrameDifference(int[][] frame, SymbolEncoder encoder, BitSink bit_sink, Symbol[] symbols)
            throws IOException {

        int width = frame.length;
        int height = frame[0].length;

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                encoder.encode(symbols[frame[x][y]], bit_sink);
            }
        }
    }
    private static int[][] decodeFrame(SymbolDecoder decoder, BitSource bit_source, int width, int height)
            throws InsufficientBitsLeftException, IOException {
        int[][] frame = new int[width][height];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                frame[x][y] = ((Unsigned8BitModel.Unsigned8BitSymbol) decoder.decode(bit_source)).getValue();
            }
        }
        return frame;
    }

    private static int[][] reconstructFrame(ArrayList<int[][]> prior_frame, ArrayList<int[][]> frame_difference) {
        int width = prior_frame.get(0).length*2;
        int height = prior_frame.get(0)[0].length*3;

        int[][] frame = new int[width][height];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (x < width/2 && y < height/3) {
                    frame[x][y] = prior_frame.get(0)[x][y] + frame_difference.get(0)[x][y];
                } else if (x < width/2 && y < 2*height/3) {
                    frame[x][y] = prior_frame.get(1)[x%400][y%150] + frame_difference.get(1)[x%400][y%150];
                } else if (x < width/2) {
                    frame[x][y] = prior_frame.get(2)[x%400][y%150] + frame_difference.get(2)[x%400][y%150];
                } else if (y < height/3) {
                    frame[x][y] = prior_frame.get(3)[x%400][y%150] + frame_difference.get(3)[x%400][y%150];
                } else if (y < 2*height/3) {
                    frame[x][y] = prior_frame.get(4)[x%400][y%150] + frame_difference.get(4)[x%400][y%150];
                } else {
                    frame[x][y] = prior_frame.get(5)[x%400][y%150] + frame_difference.get(5)[x%400][y%150];
                }
            }
        }
        return frame;
    }

    private static void outputFrame(int[][] frame, OutputStream out)
            throws IOException {
        int width = frame.length;
        int height = frame[0].length;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                out.write(frame[x][y]);
            }
        }
    }
}
