# DataSpider Servista SDK 4.2 によるアダプタ開発ガイド

## はじめに

DataSpider Servistaは、異なるシステム間のデータ連携をアイコンベースで実現できるETLツールです。標準で多くのアダプタ（データソースやプロトコルごとの接続部品）が提供されていますが、要件によってはカスタムのアダプタ開発が必要になる場合があります。本ガイドでは**DataSpider Servista Component SDK 4.2**を使用してオリジナルのアダプタを開発する方法を、Javaエンジニア初心者にも分かりやすく解説します。開発環境のセットアップから、Studio上でのプロパティ設定UIの作成、主要クラスの役割、ファイル・DB・HTTPといった種類別の実装パターン、エラー処理やトランザクション管理のベストプラクティス、そしてよくある失敗例と対策まで、ステップバイステップで説明します。最終的な目標は、**読者が自力で「Studio上でUIを伴うカスタムアダプタを1から設計・実装・デプロイ・テストできる」ようになること**です。

---

## 指示書の優先順位

プロンプトファイル＞個別の指示書＞全体の指示書

---

## フォルダ・ファイルの種類

タスクリストファイル: タスク一覧
指示書 全体の指示書(1ファイル): プロジェクト全体で共通する指示
指示書 個別の指示書(複数): 全体の指示書ではかけなかった個別の指示
指示書 プロンプトファイル(複数): タスクの実装詳細情報

---

## 振る舞い

GitHub Copilot に、以下の技術スタックに精通したエキスパートとして振る舞ってください。

* Java
* DataSpider Servista

---

## Copilotチャットへの指示方法

* 実装依頼時は「このprompt.mdの内容を実装してください」のように、参照するプロンプトファイルを明示してください。
  - 例：「prompts/HelloWorldAdapterOperation内の.prompt.mdの内容でHelloWorldAdapterOperationを実装してください」
* これによりCopilotはタスクごとの詳細仕様を最優先で反映します。
* Copilotチャットへの指示は、なるべくシンプルにしてください。

## タスク管理
* `task-list.prompt.md`はプロジェクト内の全タスク（やることリスト）を一元管理するファイルです。
* 各タスクの進捗状況（未着手/進行中/完了）を明示し、Copilotや開発者が参照します。

### 【必須】タスク進行フロー厳守ルール
1. ユーザーが「prompts/xxx.prompt.mdの内容を実装して」などの指示を出した場合、**必ず最初に「今からタスクID:xxx（内容）を実装します」とチャットで宣言すること。**
**このとき、「タスクID認識・進行ルール」の指示に従い、タスクIDは必ずtask-list.prompt.mdの表記（例：op01）をそのまま使うこと。**
   * **例: タスクID: op01**  
**タスク着手＝まず最初に必ず進行中に変更し、その後で実装を始めること**  
**実装や修正の前に、必ず進行中への変更を済ませてから作業を行うこと**

2. **タスクに着手したら、task-list.prompt.mdの該当タスクの「ステータス」列を 「タスクリストファイルの更新（必ず守ること）」の指示に従い「進行中」に必ず変更すること。**
3. 「task-list.prompt.mdのタスクID:xxxのステータスを進行中に変更しました」と報告すること。
4. prompts/xxx.prompt.mdの内容を**そのままチャットで宣言すること。要約や意訳、拡大解釈は禁止。**
   * ※「そのまま」とは、一字一句変更せず、prompt.mdファイルの本文を抜粋して貼り付けることを指す。
   * ※要約・意訳・補足・拡大解釈は禁止です。
   * ※補足説明・箇条書きへの変換などは一切行わないこと。
   * ※複数行の場合も、原文のまま貼り付けること。
   * ※prompt.mdの内容を宣言する際は、必ず「--- prompts/xxx.prompt.md の内容 ---」などの区切りを入れた後に改行して、原文をそのまま貼り付けてください。
     * 例:
     ```planetext
     --- prompts/xxx.prompt.md の内容 ---
     contextというOperationContext型の変数を作成してください。この変数は、HelloWorldAdapterOperation クラス内で使用されるインスタンス変数（フィールド）です。このクラス内で OperationContext のインスタンスを参照するために使用されます。アクセス修飾子にはprivate、オーバーライドを禁止するためfinalを使用してください。
     ```
   * ※違反があった場合は、必ず指摘・修正してください。
5. その後、実装を進める。
6. 実装や修正が完了したら、次の形式で報告すること。
   * **初回実装時は「タスクID:xxxの実装が完了しました。内容を確認してください」と報告する。**
   * 以降は「〜を変更しました。内容を確認してください。他に修正が必要な箇所があれば教えてください。」
※修正や追加の指示に対しても、必ずこの定型文で報告すること。
7.  ユーザーが「OK」「完了」「大丈夫です」「内容を確認しました、問題ないです」など、明確に“完了”を示す返答をした場合、「タスクID:xxxを完了とします」と宣言すること。
8. **task-list.prompt.mdの該当タスクの「ステータス」列を 「タスクリストファイルの更新（必ず守ること）」の指示に従い「完了」に必ず変更すること。**
9. 「task-list.prompt.mdのタスクID:xxxのステータスを完了に変更しました」と報告。

### タスクID認識・進行ルール
- タスク着手・進行時は、必ずtask-list.prompt.mdのテーブルをパースし、タスクID・内容・ステータスのリストを作成すること。
- ユーザーから指示されたタスクIDが、そのリストに**完全一致で存在**するかを必ず判定すること。
- 存在しない場合は「タスクIDが存在しません」と返し、存在する場合のみ処理を進めること。
- タスクIDの重複や表記揺れにも注意し、厳密な一致判定を行うこと。
- **この手順を省略・スキップしてはならない。**

### タスクリストファイルの更新（必ず守ること）
**タスクの進行・完了時は、必ずtask-list.prompt.mdの該当タスクの「ステータス」列を最新状態（「未着手」→「進行中」→「完了」）に更新すること。**
**この更新処理は絶対に忘れず毎回実施すること。**

- 実装や修正に着手したら、まず「進行中」に変更する
- 装や修正が完了したら、必ず「完了」に変更する
- ステータス更新を忘れた場合は、指摘・修正を徹底する
- このルールはCopilotも人間も全員厳守

なぜ必要か:
- タスクの進捗状況を常に正確に管理することで、複数人・AI・自動化ツールが同時に作業しても「誰が何をやっているか」「どこまで終わったか」が一目で分かる
- タスクの重複・漏れ・やり忘れ・未完了のまま放置…といった事故を防げる
- レビューや進捗報告、引き継ぎもスムーズになる
以上のことから、プロジェクト全体の品質・効率・透明性が大幅に向上するため。

例:
1. タスクID:01の実装を始める→task-list.prompt.mdのタスクID: 01のステータスを「進行中」に変更する
2. 実装が終わったら→task-list.prompt.mdのタスクID: 01のステータスを「完了」に変更する

### プロンプトファイルの概要と実行
1.  **プロンプトファイルの概要**:
    * 1プロンプトファイルは1機能1タスクを守ります。
    * プロンプトファイルのフォーマットは、`[タスクID]-[タスク名].prompt.md`
      * 例: op01-context_val.prompt.md

2.  **プロンプトファイルの実行**:
    * 指示されたプロンプトファイルに基づいてファイル内に書いてある指示を実行します。
    * プロンプトファイルの実行前に `task-list.prompt.md` を確認します。
    * `task-list.prompt.md`を確認したら、タスクを復唱してください。
    * プロンプトファイルの実行時は、 `copilot-instructions.md` に記述されているルールに従います。
    * プロンプトファイルの実行後、「プロンプトファイル完了後の処理」の指示に従います。

### プロンプトファイル完了後の処理

**タスクリストファイルの更新**:
- タスク完了時は必ずtask-list.prompt.mdも更新します。
- タスクの進捗状況は、task-list.prompt.mdの「ステータス」列（未着手／進行中／完了）で管理してください。
- タスクが完了したら、該当タスクの「ステータス」列を「完了」に変更してください。
- 進行中の場合は「進行中」、未着手の場合は「未着手」としてください。
- GitHub Copilot は `task-list.prompt.md` の更新が完了したらメッセージを返信してください。
  - 例: 「task-list.prompt.mdのタスクID:01のステータスを完了に変更しました。」
- 更新完了メッセージには、タスクIDやprompt.mdのパスを必ず明記してください。

---

## GitHub Copilot の動作ルール

* コンテキストが不明な場合は、質問してください。
* 存在しないライブラリや関数は使用せず、既知の、検証されたライブラリのみを使用してください。
<!-- * コードやテストで参照する前に、ファイルパスやモジュール名が存在することを確認してください。 -->
* 明示的に指示されていない限り、または `task-list.prompt.md` のタスクに含まれていない限り、既存のコードを削除または上書きしないでください。
* プロジェクトの修正や変更を行う際は、一度に 1 つのタスクに集中してください。
* コード変更後は、タスクリストファイル `task-list.prompt.md` を更新してください。
* ユーザーからの指示やprompt.mdの内容は、書かれている範囲だけを忠実に実装してください。
    * 例：「フィールドを作成」とだけあれば、フィールド宣言のみを行い、初期化や利用、コンストラクタの追加などは行わないこと。
* 指示に含まれない処理や“親切な補完”は絶対に行わないでください。
* 指示の意図や範囲に不明点がある場合は、必ずユーザーに確認してください。
*「指示書＞個別指示＞全体指示」の優先順位を守り、追加の解釈や推測はしないこと。

---

## 使用技術スタック

### 基本事項

* 言語: Java

### コードのコメント

* コードを修正したらコメントも適切なものにしてください。
* 不要なコメントは削除してください。
* 食い違いのあるコメントは修正してください。

---

## ドキュメント

* `README.md` には、使い方や説明などを記述してください。
* 変更内容は `README.md` に反映させてください。


## ディレクトリ構成
```plaintext
DSS_SDK_42/
├── .github/                      # Copilot指示・プロンプト管理
│   ├── prompts/                  # タスクごとのプロンプトファイル
│   ├── .copilot-codeGeneration-instructions.md   # コード生成の指示書
│   ├── copilot-instructions.md   # 全体の指示書
│   └── task-list.prompt.md       # タスクごとのプロンプトファイル
├── .vscode/                # VSCode用チーム共通設定
│   └── settings.json
├── dev/                    # 開発用スクリプト
│   ├── conf/
│   ├── sample_adapter/     # 開発対象のアダプタ
│   │   ├── build
│   │   ├── META-INF/
│   │   ├── src/
│   │   ├── build.xml
│   │   └── config.properties
│   ├── build.properties
│   └── build.xml
├── doc/                    # ドキュメント類
│   ├── SDKGettingStarted.adoc   # SDK導入ガイド
│   └── doc/                # APIリファレンス（HTML）
├── samples/                # サンプルアダプタ集
└── README.md
```

- **build.xml**: Antビルド用
- **src/**: Javaソース
- **META-INF/**: マニフェスト
- **samples/**: サンプルアダプタ集、新規アダプタ開発時はここを参考にします

---

## DataSpiderアダプタの基本構造

まず、DataSpiderの**アダプタ（Servista Component）**がどのような構成要素から成り立っているか、その全体像を押さえましょう。アダプタは**複数のオペレーション（処理）**および**任意のグローバルリソース**で構成され、これらをまとめる\*\*モジュールコンポーネント（AdapterModuleComponent）**というクラスがエントリーポイントになります。アダプタ内で実行される各処理は**オペレーション（Operation）**として実装し、その設定UIや入出力定義を**オペレーションファクトリ（OperationFactory）\*\*で定義します。グローバルリソースを利用する場合は、\*\*リソースファクトリ（ResourceFactory）**および**リソース（Resource）\*\*クラスを追加で実装します。以下、それぞれの役割と関係を図解します。

```plaintext
AdapterModuleComponent（アダプタ本体）
├── OperationFactory（オペレーションファクトリ）※複数可
│   └── Operation（オペレーション）
└── ResourceFactory（リソースファクトリ）※0個以上
      └── Resource（リソース）
```

上記のように、AdapterModuleComponentを中心に複数のOperationFactoryと必要に応じてResourceFactoryを持ちます。それぞれの要素の詳細を見ていきます。

### AdapterModuleComponent（アダプタ本体クラス）

**AdapterModuleComponent**はアダプタ全体を表すクラスです。具体的には、どのOperationFactoryやResourceFactoryを持つかを管理し、DataSpiderにアダプタを認識させる役割を持ちます。1つのAdapterModuleComponentに対して複数のOperationFactoryやResourceFactoryを関連付けることができ（アダプタとオペレーション/リソースは多対1の関係）、アダプタ名や表示名の定義もここで行います。AdapterModuleComponentはSDK提供の抽象クラスを継承し、**`getOperationFactories()`** メソッドでサポートするOperationFactoryの配列を、**`getResourceFactories()`** メソッドでサポートするResourceFactoryの配列を返すように実装します。

**実装ポイント:** AdapterModuleComponentの実装クラス名は規約により「`<アダプタ名>ModuleComponent`」とする必要があります（例：HelloWorldAdapterModuleComponent）。また、`getOperationFactories()`で必ずOperationFactoryのインスタンスを返すようにし、グローバルリソースを利用する場合は`getResourceFactories()`でResourceFactoryのインスタンスを返す実装を追加します。

### OperationFactory（オペレーションファクトリ）

**OperationFactory**は各オペレーション（個々の処理）のUIプロパティや入出力データ型を定義し、オペレーション実行時のインスタンスを生成するクラスです。言い換えると、\*\*「オペレーションの設計図」\*\*にあたります。
OperationFactoryでは主に次の責務を担います:

* **プロパティ定義:** Studio上の設定ダイアログに表示するプロパティ項目（テキストボックスやファイル選択など）の定義を行います。
* **入出力データ型の定義:** スクリプト間でのデータ受け渡し時に使用するデータ型（例えば入力はXMLドキュメント、出力はテキストなど）を定義します。
* **オペレーションインスタンスの生成:** 実行時にOperation（後述）のオブジェクトを作成します。

SDKではOperationFactory向けに`createOperationConfigurator(...)`や`createOperationIO(...)`といったフックメソッドが用意されており、これらを実装することで上記の役割を果たします。OperationFactory自体はインターフェースですが、よく使われるパターンに合わせて**GetDataOperationFactory**（読み取り系）や**PutDataOperationFactory**（書き込み系）などの基本実装クラスも提供されています。例えば読み取り専用のアダプタであればGetDataOperationFactoryを継承してOperationFactoryを実装できます。

**実装ポイント:** OperationFactoryの実装クラス名は通常「`<アダプタ名>OperationFactory`」のように命名します。OperationFactoryでは、`createOperationConfigurator`内でプロパティ（後述）を定義し、`createOperationIO`内で入出力データ型を定義し、`createOperation`内でOperationインスタンスを生成して返します。

### Operation（オペレーション実装）

**Operation**はアダプタの**実際の処理ロジック**を実装するクラスです。DataSpiderのスクリプト上では、1つ1つのアイコンがOperationに相当し、例えば「ファイルを読み込む」「データを変換する」「DBに書き込む」といった個別の処理内容がOperationで提供されます。

OperationはSDKのOperationインターフェースを実装し、主に\*\*`execute(Map inputData)`\*\* メソッド内に処理を記述します。DataSpider実行エンジンからこのexecuteが呼び出される際に、前段から渡されたデータが`inputData`に与えられ、戻り値のMapに処理結果データを入れて返却すると、後続のアイコンにそのデータが渡されます。

OperationはOperationFactoryに紐づいており、OperationFactoryで定義した入出力キーやデータ型に基づいて`inputData`や戻り値のMap構造を扱う必要があります。例えば、OperationFactoryで「XML入力（キー: XML\_INPUT）」を定義していれば、execute内で`inputData.get("XML_INPUT")`によりXMLデータを取得できます。また、グローバルリソースを利用する場合はOperationFactoryで設定したリソース参照キーに基づいてリソースを取得します（詳細は後述）。

**実装ポイント:** Operation実装クラス名は例えば「`<アダプタ名>Operation`」のように命名します。コンストラクタでOperationContextやOperationConfigurationを受け取り、`execute`内で`OperationConfiguration conf`（プロパティ値取得用）や`OperationContext context`（ログ出力やリソース取得用）を使用して処理を行います。複雑な処理は内部でさらにメソッドに分けても構いません。Operationは基本的にステートレスにし、必要な情報は`conf`や`context`から取得するようにするのが望ましいです。

### ResourceFactory（リソースファクトリ）と Resource（リソース）

DataSpiderでは、データベース接続や外部システムの接続先定義を**グローバルリソース**として集中管理できます。独自アダプタでグローバルリソースを利用するには、そのリソースの種類に対応した**ResourceFactory**（リソースのファクトリ）と**Resource**（リソース本体）クラスを実装します。例えば「サンプルAPIサーバ接続」用のリソースを作る場合、SampleApiResourceFactoryとSampleApiResourceを実装します。

* **ResourceFactory（リソースファクトリ）:** グローバルリソースのプロパティ設定画面とリソース名の定義、リソースの生成処理などを担当します。具体的には、DataSpider管理画面でグローバルリソースを作成する際の入力項目を定義し（例：ホスト名、ユーザ名/パスワード等）、リソース一意名（Resource名）を提供し、作成された設定に基づいてResourceインスタンスを生成する責務を持ちます。
* **Resource（リソース）:** グローバルリソースの実体クラスで、接続情報や状態を保持します。DataSpiderから見ると一種のコネクションオブジェクトです。Operationから利用され、必要に応じて接続の取得（例：DBコネクションを返す）、リクエストの送信、値の取得メソッドなどを提供します。

AdapterModuleComponentの`getResourceFactories()`でResourceFactoryを返すことで、そのアダプタは特定の種類のグローバルリソースを利用可能になります。ユーザはStudio上で先にグローバルリソース（ResourceFactoryが提供するUIに従った設定）を作成し、アダプタのプロパティからそれを参照する形で使用します。

**実装ポイント:** ResourceFactoryは`com.appresso.ds.dp.spi.ResourceFactory`インターフェースを実装します。主なメソッドは `getResourceName()`（リソース種別名の提供）、`getResourceMetaData()`（入力プロパティ項目のメタ情報提供）、`createResourceConfigurator()`（リソース作成画面UI定義）、`createResource()`（Resource本体生成）です。特に`getResourceName()`で返す文字列は、後述のOperation側プロパティでこのリソース種別を参照する際に使用するため**一意で分かりやすい名前**にしてください（例：「sample」「database」など）。Resourceは`com.appresso.ds.dp.spi.Resource`インターフェースを実装し、ResourceFactoryで定義したプロパティをフィールドに保持します。たとえばDB接続リソースなら接続文字列やユーザ情報をフィールドに持ち、`createResource()`内でJDBC接続を初期化するなどします。

---

## オペレーションプロパティ設定画面の作成

次に、**DataSpider Studio上で表示されるアダプタのプロパティ設定UI**の作り方について詳しく見ていきます。OperationFactory内で`createOperationConfigurator(...)`メソッドを実装することで、Studioでアダプタを配置した際に表示されるダイアログの項目を自由に定義できます。SDKでは様々な種類の入力コンポーネント（テキストボックス、ファイルパス選択、ドロップダウン、テーブル入力、グローバルリソース参照ボックスなど）を利用できるよう、**プロパティ制約クラス**が用意されています。以下、代表的なUIコンポーネントごとに実装例を示します。

> ※補足: コード中で`OperationConfigurator configurator = new OperationConfigurator(conf, context);`とした後に、各プロパティを**Parameter**オブジェクトとして作成し`configurator.addXXXParameter(...)`で追加する流れが基本です。ここでは各プロパティ用Parameterを生成するコードに着目します。

### テキスト入力プロパティの実装例

最も基本的な**一行テキスト入力**のプロパティは、SDKの`Fillin`クラス（単一行入力フィールド制約）を使用して実装します。例えば、HelloWorld的なメッセージ文字列をユーザに入力させたい場合、OperationFactoryに以下のようなメソッドを用意します。

```java
// プロパティ用キーの定義
static final String KEY_MESSAGE = "MESSAGE";

private SimpleParameter createMessageParameter() {
    // 文字列入力フィールドを表す制約を生成
    Fillin fillin = new Fillin();
    fillin.setLabel("メッセージ");               // ラベル表示名
    fillin.setRequired(true);                   // 必須入力に指定
    fillin.setShortcut("M");                    // Alt+Mでフォーカス（ショートカット）
    fillin.setDescription("出力メッセージを指定"); // 項目の説明
    // Parameterオブジェクトを生成して返す
    return new SimpleParameter(KEY_MESSAGE, fillin);
}
```



上記では、`Fillin`オブジェクトに対してラベルや必須フラグ、説明文などを設定し、`SimpleParameter`にラップして返しています。OperationFactoryの`createOperationConfigurator`内でこの`createMessageParameter()`を呼び出し、その戻り値を`OperationConfigurator`に追加することで、Studioのプロパティ画面に「メッセージ」という一行テキスト入力欄が表示されます。ユーザが入力した値は後で`OperationConfiguration`経由で取得可能です（例: `conf.getValue(KEY_MESSAGE)`で取得）。

### ファイル選択プロパティの実装例

ファイルパスをユーザに指定させたい場合、SDKの`FileInputFillin`クラスを使用すると便利です。`FileInputFillin`はテキスト入力＋ファイル参照ダイアログボタンが付いたUIコンポーネントです。例えば「出力先ファイル」を選択するプロパティは以下のように実装できます。

```java
static final String KEY_FILE = "FILE";

private SimpleParameter createFileInputParameter() {
    // ファイルパス入力用の制約を生成
    FileInputFillin constraint = new FileInputFillin();
    constraint.setLabel("ファイル");
    constraint.setRequired(true);
    constraint.setShortcut("F");
    constraint.setDescription("書き込み先のファイルを指定します");
    return new SimpleParameter(KEY_FILE, constraint);
}
```



`FileInputFillin`を使うことで、Studio上ではテキストボックスの右に「...」ボタン（ファイル参照ダイアログ起動）が表示されます。ユーザはボタンからローカルファイルを選択可能で、そのパスがテキストボックスにセットされます。なお必須項目に指定しているため、未入力の場合は実行時にエラーとなります。

### ドロップダウン（選択肢）プロパティの実装例

固定の選択肢から値を選ばせたい場合は`Multi`クラス（複数選択肢制約）を利用します。`Multi`はデフォルトでラジオボタンリストとして表示されますが、スタイルを変更することでコンボボックス（ドロップダウン）にもできます。例として、「インデント有無」をユーザに選択させるプロパティを作ってみましょう。選択肢は「true/false」の2値とし、UIはドロップダウン形式で表示します。

```java
static final String KEY_INDENT = "INDENT";

private SimpleParameter createIndentParameter() {
    // 選択形式のプロパティ制約を生成
    Multi multi = new Multi();
    multi.setLabel("インデント");
    multi.setRequired(true);
    multi.setShortcut("I");
    multi.setDescription("インデントを入れるかどうか指定します");
    // 選択肢を定義（true/falseの2値）
    multi.setItems(new Item[] {
        new Item("true"),
        new Item("false")
    });
    // UI表示形式をコンボボックスに設定
    multi.setStyle(Multi.STYLE_COMBOBOX);
    return new SimpleParameter(KEY_INDENT, multi);
}
```



上記では、`Multi`制約に2つのItem（文字列"true"と"false"）を設定し、`setStyle(Multi.STYLE_COMBOBOX)`でUIをプルダウンに変更しています。こうすることでStudio上では「インデント」プロパティにドロップダウンメニューが表示され、ユーザはtrue/falseを選択できます。選択結果の文字列は`conf.getValue(KEY_INDENT)`で取得でき、`"true"`/`"false"`のいずれかが得られます。もちろん必要に応じて値はBooleanに変換して利用してください。選択肢は必要に応じ増減・変更可能です。

### テーブル形式のプロパティの実装例

複数行の繰り返し入力（例えばカラムの一覧やマッピング定義など）をさせたい場合、**テーブル形式プロパティ**が有効です。SDKの`TableConstraint`クラスと`TableParameter`クラスを使うことで、表形式の入力UIを作成できます。簡単な例として、「列名の一覧」を入力させるテーブルプロパティを定義してみます。1列のテーブルで各行に列名（文字列）を入力するUIを想定します。

```java
static final String KEY_TABLE = "COLUMNS";
static final String KEY_COLUMN = "COLUMN_NAME";

public OperationConfigurator createOperationConfigurator(OperationConfiguration conf, OperationContext context) throws Exception {
    OperationConfigurator configurator = new OperationConfigurator(conf, context);

    // テーブル形式のプロパティ制約を生成
    TableConstraint tableConstraint = new TableConstraint();
    tableConstraint.setLabel("列一覧");

    // テーブルプロパティのParameterを生成
    TableParameter tableParam = new TableParameter(KEY_TABLE, tableConstraint);

    // テーブルのカラム定義を生成（列名カラム：文字列入力）
    TableColumn column = new TableColumn(KEY_COLUMN, new Fillin());
    column.getConstraint().setLabel("列名");

    // カラムをテーブルパラメータに追加
    tableParam.addColumn(column);

    // OperationConfiguratorにテーブルパラメータを追加
    configurator.addTableParameter(tableParam);
    return configurator;
}
```



`TableConstraint`にテーブル全体のラベル名「列一覧」をセットし、`TableParameter`でキー（"COLUMNS"）と制約を指定してプロパティオブジェクトを作成しています。次に`TableColumn`で列定義を追加しています。ここではカラムキー"COLUMN\_NAME"に対して`new Fillin()`を渡し、各行の入力欄を一行テキストとしています。`column.getConstraint().setLabel("列名")`でその列のヘッダ名を設定し、最後に`tableParam.addColumn(column)`でテーブルにカラムを追加しています。こうして定義したTableParameterをOperationConfiguratorに追加すると、Studio上のプロパティ画面には「列一覧」という表が表示され、ユーザは「列名」列に複数行の文字列を入力できるようになります。入力結果は`conf.getValues(KEY_TABLE)`で取得可能で、各行がMapとして格納されたリスト等の形式で得られます（詳細はSDKドキュメントの「テーブルプロパティからの値の取得」を参照してください）。

### グローバルリソース参照プロパティの実装例

カスタムアダプタでグローバルリソースを利用する場合、OperationFactory側にも**グローバルリソース参照用のプロパティ**を定義する必要があります。Studio上では、グローバルリソース参照プロパティはドロップダウン形式で、利用可能なリソースの一覧（指定した種別に一致するもの）が表示されます。SDKでは`ResourceConstraint`クラスと`ResourceParameter`クラスを使ってこれを実現します。

以下に、サンプルリソース「Sample Resource」を参照するプロパティをOperationFactory内で定義するコード例を示します。

```java
static final String KEY_SAMPLE_RESOURCE = "SAMPLE_RESOURCE";

public OperationConfigurator createOperationConfigurator(OperationConfiguration conf, OperationContext context) throws Exception {
    OperationConfigurator configurator = new OperationConfigurator(conf, context);

    // ResourceConstraintを生成
    ResourceConstraint resConst = new ResourceConstraint();
    resConst.setLabel("Sample Resource");
    // SampleResource固有のリソース名を設定（ResourceFactory側のgetResourceNameで返す名前）
    resConst.setResourceNameRegex(SampleResourceFactory.RESOURCE_NAME);

    // リソース参照パラメータを生成して追加
    ResourceParameter param = new ResourceParameter(KEY_SAMPLE_RESOURCE, resConst);
    configurator.addResourceParameter(param);
    return configurator;
}
```



上記では、`ResourceConstraint resConst`を生成しラベル「Sample Resource」を設定、さらに`setResourceNameRegex`で**参照可能なリソース種別名**を指定しています。ここで`SampleResourceFactory.RESOURCE_NAME`は、対応するResourceFactory（SampleResourceFactory）の`getResourceName()`が返すリソース種別名です（例えば`"sample"`）。このようにOperation側とResource側でリソース名を一致させることで、Studioのプロパティ画面に「Sample Resource」欄が追加され、ドロップダウンで利用可能な「sample」種別のグローバルリソースが選択できるようになります。ユーザが選択したリソース名は実行時に`conf.getValue(KEY_SAMPLE_RESOURCE)`で取得でき、それを使ってResourceインスタンスを呼び出すことができます（やり方は後述）。

> **補足:** 既存のDataSpider組み込みグローバルリソースを参照したい場合も、同様に対応するResourceFactory名（例えばデータベースなら`"database"`等）を`setResourceNameRegex`に指定すれば、カスタムアダプタから標準のDBリソース一覧を参照できます。ただし正確な名称はマニュアルで確認してください。

以上、テキスト、ファイル、選択肢、テーブル、リソース参照と主なプロパティUIの実装方法を示しました。実際のOperationFactoryではこれら複数のプロパティを組み合わせて`OperationConfigurator`に追加し、一つのダイアログ画面にまとめて表示させることが可能です。各プロパティには`setRequired(true)`や`setDescription(...)`等で適切な制約や説明を付与し、ユーザが入力ミスしにくい親切なUIを心がけましょう。

---

## ETLにおけるアダプタの責務と役割

DataSpiderアダプタは、ETL（抽出・変換・ロード）プロセスの中で「どのような責務を負うのか」を理解することも重要です。ただ開発するだけでなく、**ETL全体の文脈**でアダプタの役割を捉えることで、実装すべき機能や考慮事項が明確になります。

### DataSpiderにおけるアダプタの位置付け

DataSpiderのスクリプトは、複数のアダプタ（またはトリガー）アイコンを線で繋いでデータフローを構築します。各アダプタは単独では\*\*「データの入出力を持つ処理モジュール」**であり、前後のアダプタと組み合わせてETL処理全体を実現します。つまり、あるアダプタはデータの**抽出（Extract）**元になり、別のアダプタが変換（Transform）処理を行い、最後のアダプタが外部へ**出力（Load）\*\*する、といった形で役割分担します。

例えば、**ファイル読み取りアダプタ**は外部のファイルシステムからデータを読み出して内部形式に変換し、後続にデータを渡します。**DB書き込みアダプタ**は前段から受け取ったデータをデータベースに登録（INSERT/UPDATE）します。**変換系アダプタ**（例えば前述のGroovyスクリプトアダプタのようなもの）は、受け取ったデータを加工・フィルタリングして次に渡します。

このようにアダプタのOperationはそれぞれ\*\*「単一責務の原則」\*\*に従った1ユースケースの処理を担当します。複雑なETL処理は、複数のアダプタ（Operation）を組み合わせて段階的に実現するのがDataSpiderの設計思想です。したがってカスタムアダプタ開発時も、「自分の作るアダプタはETLのどの部分を受け持つのか」を意識しましょう。以下は典型的な役割の例です：

* **データ取得系アダプタ（リーダー）:** 外部システムからデータを取得し、DataSpiderスクリプト内に取り込む。例）ファイル読取、DBクエリ、Web APIコール（GET）など。→ **抽出 (E)** の役割
* **データ変換系アダプタ（プロセッサ）:** 入力データに対しビジネスロジック的な変換や加工を行い、結果を出力する。例）カスタムスクリプト実行、暗号化/復号、データ形式変換（XML⇔JSONなど）。→ **変換 (T)** の役割
* **データ出力系アダプタ（ライター）:** DataSpider内部のデータを外部システムに書き出す。例）ファイル書出し、DB登録、HTTP送信（POST/PUT）など。→ **ロード (L)** の役割

複合的なアダプタも存在しえますが、基本的には上記いずれか、あるいは組み合わせで役割を果たします。例えば**データベースアダプタ**は、1つのアダプタ内に「SELECTで読み取るOperation」と「INSERT/UPDATEするOperation」の**複数のOperation**を含め、両方の役割を提供することも可能です（ユーザは用途に応じてスクリプト上で使い分ける）。AdapterModuleComponentが複数OperationFactoryを返せるのはこのためです。実際、DataSpider標準の「データベース」アダプタも**参照系**（SELECT）と**更新系**（DML）の2種類のアイコンを提供しています。

### アダプタが担うべき責務の具体例

アダプタ開発時には、「何を入力に受け取り、何を出力として返すのか」を明確に設計します。加えて、以下の責務も考慮します。

* **外部システムとのインタフェース確立:** 対象システムへの接続や認証、リクエスト送信/応答受信などを適切に行う。例：ファイルパスの解決とファイルオープン、DBコネクションの取得、HTTPヘッダ設定と呼び出し。
* **データの変換・マッピング:** 外部データ形式をDataSpider内部形式（JavaオブジェクトやMap等）に変換する、またはその逆。例：CSVファイルの各行をMap（カラム名→値）にパース、DB取得結果（ResultSet）をMapのリストに変換、JSON文字列をJSONオブジェクトにパース。
* **エラー処理とリカバリ:** 外部とのやりとりで発生し得るエラーに対し、適切に対処する。例：ファイルが見つからない場合のエラー、タイムアウトや認証失敗時の再試行やエラーメッセージ出力。
* **リソースのクリーンアップ:** 接続やファイルなどを開放し、リソースリークを防ぐ。DataSpiderでは自動的に呼ばれる`destroy()`メソッドやJavaのtry-with-resourcesを活用する。
* **性能と取扱データ量:** 大量データを扱う場合のメモリ管理や処理速度。ストリーミング処理が可能ならIteratorを使う、バッファを適切に使う等。

こうした責務は一般的なアプリケーション開発と同様ですが、DataSpiderアダプタでは**OperationIOによる入出力型定義**と**OperationConfiguratorによるUI定義**を通じて、DataSpiderプラットフォームと連携する形で実装する点が特徴です。例えば「入力データがXML型である」とOperationIOで定義すれば、DataSpiderは前段の出力がXMLの場合にのみ当該アダプタを接続可能と判断したり、実行時に型チェックを行ってくれます。このように**Adapter SDKの各種仕組み（入出力制約、プロパティ制約、グローバルリソース等）を正しく活用する**ことで、堅牢で再利用性の高いアダプタを開発できるのです。

---

## 実装パターン別: アダプタ開発実例

ここからは、代表的な3種類のアダプタ（ファイル系・DB系・HTTP系）について、それぞれ実装のポイントやコード例を紹介します。実際のユースケースを意識しながら、UI定義からOperationのコード構造、データ構造まで具体的に見ていきましょう。

### 1. ファイル系アダプタの実装例

**ファイル系アダプタ**とは、ローカルまたはリモートのファイルシステムからデータを読み書きするタイプのアダプタです。ここでは「入力としてXMLデータを受け取り、指定ファイルにXMLを書き出すアダプタ」を例に考えてみます（いわゆる**ファイル出力アダプタ**）。

**UIプロパティ設計:** ユーザに指定させるべきプロパティは、**出力先ファイルパス**、**文字エンコーディング**、\*\*インデント（整形出力するか）\*\*などが考えられます。これらは既に前述した`FileInputFillin`、`CharsetNameFillinMulti`、`Multi`等で実装できます。例えばOperationFactoryに以下のようなプロパティ定義メソッドを用意します。

```java
// キー定義（例）
static final String KEY_FILE   = "FILE";
static final String KEY_CHARSET = "CHARSET";
static final String KEY_INDENT = "INDENT";

// ファイルパス
private SimpleParameter createFilePathParam() {
    FileInputFillin file = new FileInputFillin();
    file.setLabel("出力ファイル");
    file.setRequired(true);
    file.setDescription("出力先のファイルパス");
    return new SimpleParameter(KEY_FILE, file);
}
// 文字セット
private SimpleParameter createCharsetParam() {
    CharsetNameFillinMulti charset = new CharsetNameFillinMulti();
    charset.setLabel("文字コード");
    charset.setRequired(true);
    charset.setDescription("出力ファイルの文字コード");
    return new SimpleParameter(KEY_CHARSET, charset);
}
// インデント有無
private SimpleParameter createIndentParam() {
    Multi indent = new Multi();
    indent.setLabel("インデント");
    indent.setItems(new Item[]{ new Item("true"), new Item("false") });
    indent.setStyle(Multi.STYLE_COMBOBOX);
    indent.setRequired(true);
    indent.setDescription("XMLを整形して出力するか");
    return new SimpleParameter(KEY_INDENT, indent);
}
```

上記のように3つのParameter（ファイルパス、文字コード、インデント）を用意し、`createOperationConfigurator`内で`configurator.addSimpleParameter(...)`をそれぞれ呼んで追加します。これによりStudio上では、ファイルパス入力欄、文字コード選択（例えばデフォルトでUTF-8などの選択肢が表示され、必要なら直接入力も可能）、インデント有無のドロップダウンが表示されます。ユーザは例えば「出力ファイル=C:\output\result.xml、文字コード=UTF-8、インデント=true」といった設定を行えるでしょう。

**入出力データ定義:** このアダプタは**入力**としてXMLデータを受け取り、**出力**は行いません（ファイルに書き出すだけで後続には何も渡さない）と想定します。したがって、OperationFactoryの`createOperationIO`では**入力データ型: XML**、**出力データ型: なし**を定義します。SDKでは`XMLInput`や`XMLOutput`クラスが用意されています。以下に`createOperationIO`の例を示します。

```java
static final String KEY_XML_INPUT = "XML_INPUT";

public OperationIO createOperationIO(OperationConfiguration conf, OperationContext context) throws Exception {
    OperationIO io = new OperationIO();
    // 入力データがXML型であることを定義
    XMLInput input = new XMLInput(KEY_XML_INPUT);
    io.addInput(input);
    // このアダプタは出力をスクリプト上に返さない（終端）ため出力は定義しない
    return io;
}
```



ここで、入力XMLのキー"XML\_INPUT"を決め（任意の識別子）、`new XMLInput(KEY_XML_INPUT)`でXML入力制約を追加しています。これにより、前のアイコンが出力するデータがXML型でなければ接続できないような型チェックが効くようになります。戻り値のOperationIOに出力を追加していないので、後続にデータを渡さない終端Operationとなります。

**Operation実装:** Operationクラスでは、executeメソッド内で以下の処理を行います。

1. OperationConfigurationからユーザ設定値（ファイルパス、文字コード、インデント）を取得
2. OperationContextからログオブジェクトを取得（必要に応じてログ出力するため）
3. inputDataからXML入力データを取得（キー"XML\_INPUT"で取り出す）
4. ファイルに対してXMLを書き込む処理を実行
5. エラーがあれば適切にハンドリングし、必要なら例外送出
6. 正常終了時は空のMapまたは必要な情報をMapに入れてreturn（今回は出力不要なので空Map）

以下、Pseudo-code形式で例を示します。

```java
public class FileWriteXmlOperation implements Operation {
    private final OperationConfiguration conf;
    private final OperationContext context;
    public FileWriteXmlOperation(OperationContext context) {
        this.conf = context.currentConfiguration();
        this.context = context;
    }
    public Map execute(Map inputData) throws Exception {
        // 1. プロパティ値取得
        String filePath = conf.getValue(KEY_FILE).toString();
        String charset = conf.getValue(KEY_CHARSET).toString();
        boolean indent = Boolean.parseBoolean(conf.getValue(KEY_INDENT).toString());
        // 2. ログ取得
        LoggingContext log = context.log();
        // 3. 入力データ取得
        Object xmlObj = inputData.get(KEY_XML_INPUT);
        if (xmlObj == null) {
            throw new InputDataNotFoundException(KEY_XML_INPUT);
        }
        // 4. ファイル書き出し処理
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), charset))) {
            String xmlString = convertXmlObjectToString(xmlObj, indent);
            writer.write(xmlString);
            log.info("ファイルにXMLを書き出しました: " + filePath);
        } catch (Exception e) {
            log.error("ファイル書き出し中にエラー: " + e.getMessage());
            throw e;
        }
        // 5. 終了処理（今回は出力なし）
        return new HashMap();
    }
    // ※convertXmlObjectToStringはXMLオブジェクトを文字列にシリアライズする仮想メソッド
}
```

上記のように、まず`conf.getValue(KEY_FILE)`等でプロパティ値を取得しています。`inputData.get(KEY_XML_INPUT)`で前段から渡されたXMLデータを取得し、nullの場合は`InputDataNotFoundException`を投げています（この例外はSDKに用意されています）。ファイル書き込み処理ではJava標準のファイルI/Oを使用し、`try-with-resources`で安全にWriterを開き、必要に応じてXMLを文字列化して書き出しています。エラー時にはログ出力しつつ例外を再スローしています。最後に、次のアダプタに渡すデータはないため空のMapを返しています。

**補足:** ファイル書き込みアダプタの場合、書き込みの原子性や中断時の整合性も考慮しましょう。DataSpider SDKにはファイルのトランザクション管理を簡易化する`FileConnection`クラスも提供されています。今回の例では触れませんでしたが、より高度な実装ではこうした仕組みを使い、安全な書き込み（例えば一時ファイルに出力後リネーム）を行うことも可能です。

### 2. データベース系アダプタの実装例

**データベース系アダプタ**は、データベースへの問い合わせ（SELECT）やデータ操作（INSERT/UPDATE/Delete）を行うアダプタです。DataSpiderには標準アダプタがありますが、カスタムな動作や特殊なDBへの接続が必要な場合に自作することがあります。ここでは例として「任意のSQLクエリを実行して結果を取得する**DB読み取りアダプタ**」を考えます。

**UIプロパティ設計:** DBアダプタでは通常、**接続先データベース**（グローバルリソースで指定）、**実行するSQL**（テキスト入力）、必要に応じて**パラメータ**や**オプション**（例：タイムアウト時間、フェッチサイズ）などを設定させます。ここでは基本に絞り、**DB接続リソース**と**SQL文**の2つをプロパティとします。

* **DB接続リソース:** DataSpider標準の「データベース」グローバルリソースを再利用する想定で、ResourceConstraintを使い、例えば`setResourceNameRegex("database")`のように設定します（実際のリソース名は環境次第）。これによりStudioプロパティ上でDatabaseリソースを選択可能になります。コード的には先のSampleResource参照例と類似です。
* **SQL文:** 複数行になる可能性もあるので`MultipleLineFillin`（複数行テキストエリア）制約を使用します。例えば以下のようにします。

```java
static final String KEY_SQL = "SQL";

private SimpleParameter createSqlParam() {
    MultipleLineFillin multiLine = new MultipleLineFillin();
    multiLine.setLabel("SQL文");
    multiLine.setRequired(true);
    multiLine.setRows(5);
    multiLine.setDescription("実行するSQLクエリを入力します");
    return new SimpleParameter(KEY_SQL, multiLine);
}
```

このプロパティでユーザはSELECT文など任意のSQLを入力できます。なお、パラメータ埋め込み等が必要な場合は別途入力欄を増やすか、SQL内にプレースホルダを決めておきスクリプト中でマッピングする等の対応が考えられますが、ここでは割愛します。

**入出力データ定義:** 今回の読み取りアダプタは**出力としてDB結果データ**を返す必要があります。DataSpiderでは一般に**レコードリスト（複数件の結果）**を扱う際、各レコードをMap（カラム名→値）にし、それらのリストをJavaのIterableや専用クラスでカプセル化して扱います。SDKには例えば`RecordListOutput`や`TableOutput`のようなクラスがありそうですが、簡単のため**出力をXML型**とすることにします。これは、DB結果をXMLドキュメントに変換して返すイメージです（例えば<records><record>...の形式）。入出力定義としては**入力なし**（外部からの入力データは不要）、**出力: XML**となります。OperationFactoryの`createOperationIO`では以下のようになります。

```java
static final String KEY_XML_OUTPUT = "XML_OUTPUT";

public OperationIO createOperationIO(OperationConfiguration conf, OperationContext context) throws Exception {
    OperationIO io = new OperationIO();
    // 出力データがXML型であることを定義
    XMLOutput output = new XMLOutput(KEY_XML_OUTPUT);
    io.addOutput(output);
    return io;
}
```

これで、後続のアダプタにはXMLデータが渡されることになります。もちろん、別の表現で出力したい場合（例えばCSVテキストやレコードリスト型）、対応するOutput制約クラスを選んでください。

**Operation実装:** Operationでは、以下の手順で処理します。

1. プロパティから**選択されたDBリソース名**と**SQL文**を取得。
2. OperationContextから`context.getResource(resourceName)`を呼び出し、対応するResource（ここではDB接続リソース）インスタンスを取得。
3. Resourceインスタンスからコネクションを取得（例えば`resource.getConnection()`がある想定）。取得できなければ例外。
4. コネクション上でSQLを実行（JDBCを直接使う）。結果のResultSetを展開して全レコード取得。
5. 結果をXMLドキュメントオブジェクトに変換。
6. ログ出力やリソース開放（コネクション閉じる or リソースに返却）。
7. 戻り値MapにXMLドキュメントを設定してreturn。キーはOperationIOで定義した"XML\_OUTPUT"とします。

以下、擬似コード例です。

```java
public class DatabaseSelectOperation implements Operation, Transactional {
    private final OperationConfiguration conf;
    private final OperationContext context;
    public DatabaseSelectOperation(OperationContext context) {
        this.conf = context.currentConfiguration();
        this.context = context;
    }
    public Map execute(Map inputData) throws Exception {
        // 1. プロパティ取得
        String resourceName = conf.getValue(KEY_SAMPLE_RESOURCE).toString();  // 選択されたDBリソース名
        String sql = conf.getValue(KEY_SQL).toString();
        // 2. リソース取得
        DatabaseResource dbResource = (DatabaseResource) context.getResource(resourceName);
        if (dbResource == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        Connection conn = null;
        try {
            // 3. コネクション取得
            conn = dbResource.getConnection();
            // オートコミットオフ（トランザクション管理の場合）
            conn.setAutoCommit(false);
            // 4. SQL実行
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // 5. 結果をXMLに変換
            org.w3c.dom.Document xmlDoc = convertResultSetToXml(rs);
            // 6. ログ出力
            context.log().info("SQL実行完了。結果件数:" + getRowCount(rs));
            // 7. コネクションは後でクローズ（下のfinallyで）
            // 8. 戻り値にXMLを設定
            Map<String, Object> output = new HashMap<>();
            output.put(KEY_XML_OUTPUT, xmlDoc);
            return output;
        } catch (SQLException e) {
            context.log().error("SQL実行中にエラー: " + e.getMessage());
            // トランザクションロールバック
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            // コネクション返却（プールへ）
            if (conn != null) {
                conn.commit(); // コミット
                conn.close();  // 接続をプールへ返す（実装による）
            }
        }
    }
}
```

この例では、Operationが`Transactional`インターフェースも実装しています。DataSpiderではOperationがTransactionalを実装すると、実行前後でトランザクションの開始終了処理を自動で呼び出してくれる仕組みがあります。上記では手動でcommit/rollbackしていますが、より厳密にはResource側やTransaction管理の仕組みと連携できます。ともあれ、ポイントは`context.getResource(resourceName)`で取得したResource（ここではDatabaseResource）を介してコネクションを取得し、標準JDBCでクエリを実行している点です。`ResourceNotFoundException`のスローも忘れずに行っています。結果をXMLドキュメントに変換する処理（`convertResultSetToXml`）は実装次第ですが、各行をXMLの<record>要素にし、その子要素に各カラムの値を書き込むような実装になるでしょう。

**補足:** 実際にはDataSpiderの内部型で**レコード集合**を扱うクラスが存在し、それを使う方が自然な場合もあります（組み込みDBアダプタではResultSetを独自のRecordList構造にして後続マッパーで扱いやすくしています）。しかしカスタムアダプタではXMLやJSONなど汎用フォーマットに乗せてしまうのも手です。どちらにせよ、**出力のデータ型と構造をどうするか**はアダプタ設計時に検討すべき重要事項です。

### 3. HTTP系アダプタの実装例

**HTTP系アダプタ**は、REST APIやWebサービスとの通信を行うアダプタです。こちらもDataSpiderにはRESTアダプタ等がありますが、独自プロトコルや特殊な認証フローがある場合に自作することがあります。例として「指定したURLにHTTP GETリクエストを発行し、レスポンスボディをテキストとして取得するアダプタ」を考えてみます。

**UIプロパティ設計:** 必要なプロパティは**リクエストURL**と**タイムアウト**程度にしましょう。さらに認証トークンなど必要なら追加しますが、ここではシンプルにします。

* **URL:** ユーザに入力させる。単純なテキスト入力（Fillin）でOKです。バリデーションを追加するなら、`RegexConstraint`でURL形式チェックをかけることもできますが詳細は割愛。
* **タイムアウト(ms):** 数値入力。これは`NumberFillin`制約を使うと良いでしょう。setMinimum/Maximumで範囲制限も可能です。

```java
static final String KEY_URL = "URL";
static final String KEY_TIMEOUT = "TIMEOUT";

private SimpleParameter createUrlParam() {
    Fillin url = new Fillin();
    url.setLabel("リクエストURL");
    url.setRequired(true);
    url.setDescription("HTTPリクエストを送る先のURL");
    return new SimpleParameter(KEY_URL, url);
}
private SimpleParameter createTimeoutParam() {
    NumberFillin number = new NumberFillin();
    number.setLabel("タイムアウト(ms)");
    number.setRequired(true);
    number.setDefaultValue("30000"); // デフォルト30秒
    number.setDescription("リクエストのタイムアウト時間（ミリ秒）");
    return new SimpleParameter(KEY_TIMEOUT, number);
}
```

**入出力データ定義:** このアダプタは外部からの入力は取らず（URL先がデータ源）、**出力**として取得したレスポンスデータを返します。レスポンスが例えばJSON文字列だとすると、出力型をテキスト（文字列）とするか、パースしてXMLやレコードにして返すか選択肢があります。ここではシンプルに**出力: テキスト**とします。SDKに`StringOutput`のような制約があるかは未確認ですが、Outputを指定せずとも戻り値Mapに文字列を入れればDataSpider上では文字列型として扱われます（内部的にはObject型）。正確を期すならカスタムの型を定義することもできますが、ここでは割愛します。

**Operation実装:** HTTP通信はJavaではHttpURLConnectionやHttpClient等で実装できます。ここではHttpURLConnectionを用いた簡素な実装を示します。

```java
public class HttpGetOperation implements Operation {
    private final OperationConfiguration conf;
    private final OperationContext context;
    public HttpGetOperation(OperationContext context) {
        this.conf = context.currentConfiguration();
        this.context = context;
    }
    public Map execute(Map inputData) throws Exception {
        String urlStr = conf.getValue(KEY_URL).toString();
        int timeout = Integer.parseInt(conf.getValue(KEY_TIMEOUT).toString());
        LoggingContext log = context.log();
        HttpURLConnection conn = null;
        try {
            // URL接続設定
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            // 必要なヘッダがあれば conn.setRequestProperty で設定
            conn.connect();
            int status = conn.getResponseCode();
            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                        .lines().collect(Collectors.joining("\n"));
            log.info("HTTP GET実行: " + urlStr + " ステータス:" + status);
            // 出力データを設定
            Map<String,Object> output = new HashMap<>();
            output.put("RESPONSE_BODY", responseBody);
            output.put("STATUS_CODE", status);
            return output;
        } catch(Exception e) {
            log.error("HTTP通信エラー: " + e.getMessage());
            throw e;
        } finally {
            if(conn != null) conn.disconnect();
        }
    }
}
```

上記では、プロパティから取得したURL文字列とタイムアウト値を使ってHTTP接続を行い、レスポンスステータスコードとボディを読み取っています。文字コードはUTF-8固定にしていますが、必要ならプロパティ化するかレスポンスヘッダのContent-Typeを見て判断します。取得したresponseBodyは文字列としてマップに格納し、キー"RESPONSE\_BODY"で返しています。またステータスコードも参考までに入れています。OperationIOで出力定義していないため、キーは任意ですが、後続で使う場合は決めておいた方がよいでしょう（例えばテキスト出力ならKEY\_TEXT\_OUTPUTのように）。

**補足:** HTTPアダプタでは、GET以外にPOSTやPUTなどのメソッド、リクエストボディの扱い、認証（Basic認証やOAuth2など）、SSL証明書、プロキシ対応など考慮事項が多岐にわたります。実装パターンも一様ではありません。必要に応じてプロパティを増やし、Operation内で上記設定を行ってください。グローバルリソースとして「HTTP接続先」（ベースURLや認証トークン保持）を実装し、ResourceConstraintで選択させるようにすると複数スクリプトで再利用できて便利です。

---

## エラー処理・ロギング・トランザクション管理・グローバルリソース再利用のベストプラクティス

堅牢なアダプタを実装するには、エラー時の対処やログ出力、トランザクション管理、リソースの再利用戦略についても注意を払う必要があります。ここでは、それぞれについてベストプラクティスをまとめます。

* **エラー処理:**
  アダプタ内部で例外が発生した場合、基本的には上位（DataSpiderエンジン）にその例外を伝播させ、スクリプトのエラーとして扱わせます。`execute`メソッドでExceptionをthrowsしていれば、throwした時点で当該アイコンはエラー終了し、以降の処理はスキップされます。これにより全体のトランザクションもロールバックされます。したがって、**致命的なエラーはcatchせずthrowする**のが基本です。例えば「入力データが見つからない」「必須プロパティがnull」等は`InputDataNotFoundException`や`IllegalArgumentException`など適切な例外をスローします。リソース参照が見つからない場合は先述のように`ResourceNotFoundException`を投げます。一方、対処可能なエラー（リトライできるものなど）は、内部でリトライ処理を行うか、エラー内容をより詳細にしてthrowすると良いでしょう。また、DataSpiderの独自例外（例えば`InputDataNotFoundException`等）を投げるとログやUIに分かりやすく表示される利点があります。

* **ロギング:**
  アダプタ内での処理状況やエラーは、必ずDataSpiderのログ機構に記録しましょう。OperationContextの`log()`から取得できるLoggingContextを使うことで、DataSpider標準のログに出力されます。例えば`context.log().info("処理開始")`や`context.log().error("エラー詳細", e)`のように埋め込んでおけば、実行時に**studio.log**や**ds.log**に記録されデバッグに役立ちます。特にcatchブロックで例外を握りつぶさず`log.error`でスタックトレースを出力してからthrowするのは重要です。ログレベルは適切に（通常情報はINFO、想定内軽微エラーはWARN、深刻なものはERROR）使い分けましょう。なお、グローバルリソース側でも`ResourceContext.log()`等からログ出力可能です。

* **トランザクション管理:**
  DataSpiderは複数アダプタ間でのトランザクションをサポートしています。具体的には、アダプタが`XATransaction`（2相コミット）や`LocalTransaction`（単相コミット）インターフェースを実装している場合、スクリプト実行時にトランザクションを開始・終了してくれます。例えばDB更新系アダプタでLocalTransactionを実装すれば、スクリプト全体で1回のCOMMIT/ROLLBACKを行えます。自作アダプタでこれを利用するには、OperationまたはResourceで該当インターフェースを実装し、`begin()`や`commit()`,`rollback()`の処理を記述します。もしくはOperationでTransactionalを実装するだけで、自動的に各処理前後にbegin/commitを呼んでくれる簡易モードもあります。トランザクション管理が必要なアダプタ（主にDBや一連の操作をまとめたいケース）では、こうした仕組みを活用するとよいでしょう。また、トランザクションに参加するResource（例えばXA対応データソース）は`com.appresso.ds.dp.spi.XAResourceParticipant`等のインターフェースを実装することで、DataSpiderの分散トランザクションに組み込むこともできます。逆にトランザクション不要な場合は何も実装しなければ自動的にNoTransaction扱いです。重要なのは、**複数リソースを扱う場合の整合性**と**失敗時の部分完了防止**です。例えばファイルとDB両方を書き込むアダプタなら、途中失敗時に片方だけ書き込まれてもう片方が未実行、という不整合を起こさないように工夫します（トランザクションで括る、または一時領域に出力して最後に同時反映する等）。

* **グローバルリソースの再利用:**
  グローバルリソースは使いまわすことで、**接続のオーバーヘッド削減**や**設定管理の一元化**が可能です。自作アダプタでも極力既存のリソース定義を活用するか、共通化できるものはグローバルリソース化しましょう。例えば複数のアダプタで使うAPIのエンドポイントURLや認証情報は、カスタムResourceとして一箇所で設定し、それぞれのアダプタから参照する形にすると保守が楽になります。ResourceFactory/Resourceを実装する際は、**同じ設定のリソースは同一インスタンスを共有**できるよう`getResourceMetaData()`できちんと比較情報を返すのがポイントです。DataSpiderはResourceMetaData（リソース設定の識別情報）が同一ならインスタンスをプールし再利用します。例えばDB接続なら、ホスト・ユーザ・DB名が全て同じResourceは1つのコネクションプールにまとめる、といった挙動です。Resourceの`destroy()`実装も重要で、プール破棄時のクリーンアップを適切に行いましょう。

  > **補足:** グローバルリソース参照プロパティをOperationに追加する際、`setResourceNameRegex`で広範な名前を指定すれば複数種類のリソースを受け付けることも可能です（例：`".*"`とすれば全種類から選択できる）。しかし通常は誤選択を避けるため絞り込む方が望ましいです。例えば「DB or APIどちらのリソースでもよい」といった可変な場合でも、UI上は別々のプロパティにする方がユーザには親切でしょう。

* **パフォーマンス上の工夫:**
  最後に付け加えると、エラーやトランザクションとは異なりますが**大容量データ処理**におけるベストプラクティスにも触れておきます。データ件数が非常に多い場合、一括でメモリに載せるとOOM（メモリ不足）になりかねません。SDKではストリーミング処理向けにIteratorパターンを使った`LargeData`関連のAPIも用意されています（詳細はSDKドキュメント「9. 大容量データ処理」を参照）。必要であればこうしたものも検討してください。

---

## よくある失敗例とその対策

カスタムアダプタ開発で新人エンジニアが陥りがちなミスと、その防止策をまとめます。以下のチェックリストに沿って自分の実装を見直すことで、トラブルシューティングが容易になるでしょう。

* **(1) プロパティのキー名不一致:** OperationFactoryで定義したプロパティキーと、Operationで取得するキー名が食い違っているケースです。例えばプロパティを`KEY_FILENAME`で登録したのに、Operationで`conf.getValue("FILE_NAME")`と別のキーを使っていると、常にnullになりエラーになります。これを防ぐには、**キーを`static final`定数で管理し、定義と使用で同じものを参照する**ことです。また、スペルミスにも注意しましょう。特に入出力データのキーについてはOperationIO定義とも一致させる必要がありますので、コピーペーストか共通定数化が有効です。

* **(2) OperationIO未定義や不整合:** 入力を使うのにOperationIOで定義していない、あるいは定義したのにOperation側で使っていない、といった不整合です。前者の場合、実行時に`InputDataNotFoundException`が発生するか、Studioでそもそも前後接続できないこともあります。後者の場合、無駄なデータ取得をしていることになりパフォーマンス低下を招きます。**OperationIOで定義したキー＝Operation内で使用するキー**を徹底してください。不要な入出力は定義しないことも大切です。

* **(3) Resource未登録・未参照:** グローバルリソースを使うアダプタで、AdapterModuleComponentの`getResourceFactories()`に自作ResourceFactoryを返していない、あるいはOperationFactory側でResourceConstraintを定義していないケースです。この場合、Studio上でリソース参照プロパティが出ず、利用したいリソースが選択できません。また、ResourceConstraintの`setResourceNameRegex`に誤った名前を指定していてマッチしない場合も、ドロップダウンが空になってしまいます。対策として、**AdapterModuleComponentにResourceFactory配列を正しく返す**、および**ResourceFactory.getResourceName()で返す文字列とResourceConstraintの正規表現を一致させる**ことを確認してください。さらに、Operation側で`context.getResource(name)`する際のnameも同一である必要があります。サンプルではRESOURCE\_NAME=`"sample"`で統一していましたが、自分の実装でも一貫性を持たせましょう。

* **(4) データ型ミスマッチ:** 入力データや出力データの型を誤って扱うミスです。例えば、InputをXML型にしたのに実際にはJSON文字列が渡ってきてパースエラーになる、といったケースがあります。このようなとき、原因究明が難しくなります。**データ型は事前にOperationIOで厳密に指定**し、Operation内でも`instanceof`チェックや適切なキャストを行いましょう。どうしても可変な型を扱う場合は、Objectとして処理して自前で型判定するか、可能なら型ごとに別Operationに分ける設計にします。また、出力についても後続で期待する型と合っているか確認が必要です。たとえば後続が「JSONパーサ」アダプタならテキスト(JSON文字列)を出力すべきで、XMLドキュメントを返したのでは繋がりません。

* **(5) ライフサイクル管理ミス:** Connectionやファイルを開いたまま閉じない、スレッドプールを停止しないなど、リソースのクリーンアップ漏れです。これはJava全般の問題ですが、DataSpider上で動く場合リソースリークは長時間動作で効いてきます。必ず`finally`でcloseする、try-with-resourcesを使う、Resourceのdestroyで停止処理をする、といった対応を取ってください。また、AdapterModuleComponentやOperationには`destroy()`メソッドもあり、スクリプト完了時に呼ばれるので、必要ならオーバーライドして後片付けを実装します。

* **(6) アイコン表示やメッセージ定義忘れ:** 実装とは直接関係ありませんが、Studio上でのアイコン（小さいアイコンと大きいアイコン）や、ヘルプの表示名/説明を定義するXMLファイルの用意を忘れるケースがあります。SDKではコンポーネント定義XML（module.xml など）で表示名やアイコンパスを指定する必要があります。これを忘れるとStudioに配置はできても無名だったり?アイコンになります。開発後は**コンポーネント定義XML**を確認し、必要事項（クラス名、ラベル、アイコンパス、バージョン等）を正しく記述しましょう。

---

## 参考リンク・ドキュメント

- DataSpider SDK Getting Started: `../doc/SDKGettingStarted.adoc`
- APIリファレンス: `../doc/api`
- サンプル: `../samples/`
- [公式サポートサイト](https://www.hulft.com/support/dataspider/)

---


## まとめ: カスタムアダプタ開発のステップ

本ガイドでは、DataSpider Servista SDK 4.2を用いたアダプタ開発の基礎から実践までを詳説しました。最後に、実際にカスタムアダプタを作成する際の大まかな手順を整理します。

1. **要求定義と設計:** まずどんなアダプタが必要か、入出力やプロパティ、利用するリソース、処理内容を明確にします。ETL全体での役割や既存アダプタとの差異も洗い出します。設計段階で、本ガイドで説明した\*\*主要クラス（AdapterModuleComponent, OperationFactory, Operation, ResourceFactory, Resource）\*\*のどれが必要かを決め、クラス構成を考えます。UML図を書いてみるのもよいでしょう。

2. **開発環境セットアップ:** Eclipseベースの開発がおすすめです（SDK 4.2用のEclipseプラグインも提供されています）。SDKのjarや必要ライブラリをプロジェクトに追加し、Java 8でコーディングします（Servista4.2はJava8対応）。ユーザガイドのサンプルを読み込みながら進めると理解が深まります。

3. **コード実装:** AdapterModuleComponentから順にJavaクラスを実装していきます。OperationFactoryでプロパティUIとIO定義、Operationでビジネスロジック、必要ならResourceFactory/Resourceでリソース管理を実装します。途中コンパイルエラーが出たらSDKのクラス参照やインポート漏れを確認します。適宜単体テスト（Junit等）で`execute`メソッドの挙動を検証すると安心です。

4. **ビルドとデプロイ:** 実装ができたらJarファイルをビルドします。SDKサンプルではAntやMavenを使う例がありますが、手動でもOKです。完成したJarと定義XML、アイコンファイルを**DataSpiderサーバの所定のディレクトリ**に配置します。通常は`DataSpiderホーム/dsud/adapter`配下にフォルダを作って配置し、サーバを再起動するとアダプタが読み込まれます（詳しくはSDK開発ガイドの「アダプタのデプロイ」を参照）。

5. **Studioでテスト:** DataSpider Studioを起動し、新しいスクリプトで開発したアダプタがパレットに表示されていることを確認します。表示名やアイコンが正しく出ていれば成功です。スクリプトキャンバスに配置し、入出力を他の適当なアダプタと繋いでみましょう。プロパティ画面が期待通り表示され、設定できるか確認します。実行ボタンでスクリプトを実行し、正常に動けばOKです。うまくいかない場合はStudioのログ（studio.log）やサーバログ（ds.log）を確認し、ログ出力した内容やスタックトレースから原因を追求します。

6. **ドキュメント整備:** アダプタの使い方や制約、設定例などを簡単にまとめてチーム内に共有しましょう。将来的なメンテナンスや他者利用のためにも、ヘルプファイル（SDKではヘルプXMLも作成できます）を用意できるとベターです。

以上のステップを踏めば、オリジナルアダプタの開発からデプロイ、活用まで一通り遂行できるはずです。最初は難しく感じるかもしれませんが、本ガイドと公式SDKドキュメントのサンプルコードを照らし合わせながら進めれば理解が深まります。是非チャレンジして、DataSpiderの機能を拡張する自作アダプタ開発に挑戦してみてください。きっとデータ連携の幅がさらに広がることでしょう。お疲れ様でした！
