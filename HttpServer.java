import java.io.*;
import java.net.*;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started on port 8080");
        System.out.println("http://localhost:8080/");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            String request = in.readLine();
            if (request != null && request.startsWith("GET")) {
                String[] requestParts = request.split(" ");
                String url = requestParts[1];
                String expression = "";

                if (url.length() > 1) {
                    expression = url.substring(1);
                }

                if (expression.endsWith("=")) {
                    expression = expression.substring(0, expression.length() - 1);
                    String result = evaluateExpression(expression);
                    expression = result;
                }

                if (expression.equals("C")) {
                    expression = "";
                }

                String htmlResponse = generateHtmlResponse(expression);
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" + htmlResponse;
                out.write(response.getBytes());
            }

            out.close();
            in.close();
            clientSocket.close();
        }
    }

    private static String evaluateExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "0";
        }

        try {
            // Remove all whitespace
            expression = expression.replaceAll("\\s+", "");

            double result = evaluate(expression);
            return Double.toString(result);
        } catch (Exception e) {
            return "Error";
        }
    }

    // parser
    private static double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean consumeChar(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (consumeChar('+')) x += parseTerm(); // addition
                    else if (consumeChar('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (consumeChar('*')) x *= parseFactor();
                    else if (consumeChar('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (consumeChar('+')) return parseFactor();
                if (consumeChar('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }

    private static String generateHtmlResponse(String currentExpression) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Calculator</title>\n" +
                "    <style>\n" +
                "        body {display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f4f4f4; }\n" +
                "        #Calculator { background-color: #fff; border: 1px solid #ccc; border-radius: 10px; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); width: 300px; }\n" +
                "        #expression { margin-bottom: 20px; font-size: 24px; text-align: right; padding: 10px; border: 1px solid #ccc; border-radius: 5px; background-color: #f9f9f9; }\n" +
                "        #btnHolder { display: flex; gap: 10px; }\n" +
                "        .numberButtons, .operationButtons { display: flex; flex-direction: column; gap: 10px; }\n" +
                "        .rowOne, .rowTwo, .rowThree, .rowFour { display: flex; gap: 10px; }\n" +
                "        a { padding: 15px; font-size: 18px; text-decoration: none; color: black; border: 1px solid #ccc; border-radius: 5px; text-align: center; cursor: pointer; background-color: #f1f1f1; flex: 1; transition: background-color 0.3s ease; }\n" +
                "        a:hover { background-color: #ddd; }\n" +
                "        #clear { background-color: #ff6b6b; color: white; }\n" +
                "        #clear:hover { background-color: #ff4c4c; }\n" +
                "        #equals { background-color: #4CAF50; color: white; }\n" +
                "        #equals:hover { background-color: #45a049; }\n" +
                "        #plus, #minus, #divide, #multiply { background-color: #4CAF50; color: white; }\n" +
                "        #plus:hover, #minus:hover, #divide:hover, #multiply:hover { background-color: #45a049; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"Calculator\">\n" +
                "        <h3>Calculator</h3>" +
                "        <div id=\"expression\">" + currentExpression + "</div>\n" +
                "        <div id=\"btnHolder\">\n" +
                "            <div class=\"numberButtons\">\n" +
                "                <div class=\"rowOne\">\n" +
                "                    <a href=\"/" + currentExpression + "1\">1</a>\n" +
                "                    <a href=\"/" + currentExpression + "2\">2</a>\n" +
                "                    <a href=\"/" + currentExpression + "3\">3</a>\n" +
                "                </div>\n" +
                "                <div class=\"rowTwo\">\n" +
                "                    <a href=\"/" + currentExpression + "4\">4</a>\n" +
                "                    <a href=\"/" + currentExpression + "5\">5</a>\n" +
                "                    <a href=\"/" + currentExpression + "6\">6</a>\n" +
                "                </div>\n" +
                "                <div class=\"rowThree\">\n" +
                "                    <a href=\"/" + currentExpression + "7\">7</a>\n" +
                "                    <a href=\"/" + currentExpression + "8\">8</a>\n" +
                "                    <a href=\"/" + currentExpression + "9\">9</a>\n" +
                "                </div>\n" +
                "                <div class=\"rowFour\">\n" +
                "                    <a href=\"/" + currentExpression + "0\">0</a>\n" +
                "                    <a href=\"/" + currentExpression + ".\">.</a>\n" +
                "                    <a href=\"/\">C</a>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class=\"operationButtons\">\n" +
                "                <a href=\"/" + currentExpression + "+\">+</a>\n" +
                "                <a href=\"/" + currentExpression + "-\">-</a>\n" +
                "                <a href=\"/" + currentExpression + "/\">/</a>\n" +
                "                <a href=\"/" + currentExpression + "*\">*</a>\n" +
                "                <a href=\"/" + currentExpression + "=\">=</a>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}